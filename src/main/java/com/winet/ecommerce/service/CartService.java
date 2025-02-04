package com.winet.ecommerce.service;

import com.winet.ecommerce.exception.custom.ApiException;
import com.winet.ecommerce.exception.custom.ResourceNotFoundException;
import com.winet.ecommerce.model.Cart;
import com.winet.ecommerce.model.CartItem;
import com.winet.ecommerce.model.Product;
import com.winet.ecommerce.payload.dto.CartDTO;
import com.winet.ecommerce.payload.dto.ProductDTO;
import com.winet.ecommerce.payload.response.CartResponse;
import com.winet.ecommerce.repository.CartItemRepository;
import com.winet.ecommerce.repository.CartRepository;
import com.winet.ecommerce.repository.ProductRepository;
import com.winet.ecommerce.repository.UserRepository;
import com.winet.ecommerce.util.DtoUtils;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Supplier;

import static com.winet.ecommerce.util.PagingAndSortingUtils.getPageDetails;

@Service
public class CartService {

	private final ProductRepository productRepository;
	private final CartRepository cartRepository;
	private final CartItemRepository cartItemRepository;
	private final UserRepository userRepository;
	private final ModelMapper modelMapper;
	private final DtoUtils dtoUtils;

	@Autowired
	public CartService(ProductRepository productRepository, CartRepository cartRepository, CartItemRepository cartItemRepository, UserRepository userRepository, ModelMapper modelMapper, DtoUtils dtoUtils) {
		this.productRepository = productRepository;
		this.cartRepository = cartRepository;
		this.cartItemRepository = cartItemRepository;
		this.userRepository = userRepository;
		this.modelMapper = modelMapper;
		this.dtoUtils = dtoUtils;
	}

	public CartResponse getAll(Integer page, Integer size, String sort, String order) {
		Pageable pageDetails = getPageDetails(page, size, sort, order);
		return getPaginatedAndSortedProductResponse(() -> cartRepository.findAll(pageDetails));
	}

	public CartDTO addProduct(Long productId, Integer quantity) {

		if(quantity <= 0) throw new ApiException("Product quantity must be greater than 0");

		Cart userCart = createCart();

		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

		CartItem cartItem = cartItemRepository.findByProductIdAndCartId(productId, userCart.getCartId())
				.orElse(null);

		if(cartItem != null) throw new ApiException("Product already exists in the cart");

		cartItem = new CartItem();
		cartItem.setProduct(product);
		cartItem.setCart(userCart);
		cartItem.setQuantity(quantity);
		cartItem.setDiscount(product.getDiscount());
		cartItem.setProductPrice(product.getSpecialPrice());

		cartItemRepository.save(cartItem);

		userCart.setTotalPrice(userCart.getTotalPrice().add(
				cartItem.getProductPrice()
						.multiply(BigDecimal.valueOf(quantity)))
		);

		cartRepository.save(userCart);

		CartDTO cartDTO = modelMapper.map(userCart, CartDTO.class);

		userCart.getCartItems().add(cartItem);
		List<CartItem> cartItems = userCart.getCartItems();

		List<ProductDTO> productDTOList = cartItems.stream()
				.map(ci -> modelMapper.map(ci.getProduct(), ProductDTO.class))
				.toList();

		cartDTO.setProducts(productDTOList);

		return cartDTO;
	}

	@Transactional
	public CartDTO updateProductQuantity(Long cartId, Long productId, String operation) {

		// TODO
		Cart userCart = createCart();
		// retrieve user Cart from authenticated user
		// check if the Cart user is the same as the cart from authenticated user or the role is ADMIN

		int valueToChange = operation.equalsIgnoreCase("add") ? 1 : -1;

		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

		CartItem cartItem = cartItemRepository.findByProductIdAndCartId(productId, cartId)
				.orElseThrow(() -> new ApiException(product.getProductName() + " is not in the cart"));

		cartItem.setQuantity(cartItem.getQuantity() + valueToChange);
		cartItem.setDiscount(product.getDiscount());
		cartItem.setProductPrice(product.getSpecialPrice());

		userCart.setTotalPrice(userCart.getTotalPrice()
				.add(
						cartItem.getProductPrice()
								.multiply(BigDecimal.valueOf(cartItem.getQuantity())))
		);

		CartItem updatedCartItem = cartItemRepository.save(cartItem);

		if(updatedCartItem.getQuantity() == 0) {
			cartItemRepository.delete(updatedCartItem);
			userCart.getCartItems().remove(updatedCartItem);

			if(userCart.getCartItems().isEmpty()) {
				cartItemRepository.delete(productId, userCart.getCartId());
			}
		}

		return dtoUtils.convertToDTO(userCart);
	}

	@Transactional
	public CartDTO deleteProduct(Long cartId, Long productId) {

		// TODO
		// modify with Cart from authenticated user
		// check if the Cart user is the same as the cart from authenticated user or the role is ADMIN
		Cart userCart = cartRepository.findById(cartId)
				.orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));

		CartItem cartItem = cartItemRepository.findByProductIdAndCartId(productId, cartId)
				.orElseThrow(() -> new ApiException("Product is not in the cart"));

		cartItemRepository.delete(productId, userCart.getCartId());
		userCart.setTotalPrice(userCart.getTotalPrice().subtract(
				cartItem.getProductPrice()
						.multiply(BigDecimal.valueOf(cartItem.getQuantity()))
		));
		userCart.getCartItems().remove(cartItem);

		return dtoUtils.convertToDTO(userCart);
	}

	private Cart createCart() {

		// TODO
		// retrieve user cart from authenticated user

		Cart cart = new Cart();
		cart.setTotalPrice(BigDecimal.ZERO);

		return cartRepository.save(cart);
	}


	private CartDTO getForUser(String email, Long cartId) {
		Cart cart = cartRepository.findByUserEmailAndCartId(email, cartId)
				.orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));
		return dtoUtils.convertToDTO(cart);
	}


	private CartResponse getPaginatedAndSortedProductResponse(Supplier<Page<Cart>> query) {
		Page<Cart> productsPage = query.get();
		List<Cart> allCarts = productsPage.getContent();

		if(allCarts.isEmpty()) throw new ApiException("No carts found");

		List<CartDTO> cartDTOs = allCarts.stream()
				.map(dtoUtils::convertToDTO)
				.toList();

		return new CartResponse(
				cartDTOs,
				productsPage.getNumber(),
				productsPage.getSize(),
				productsPage.getTotalElements(),
				productsPage.getTotalPages(),
				productsPage.isLast()
		);
	}

}

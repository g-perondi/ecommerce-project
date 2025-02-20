package com.winet.ecommerce.service;

import com.winet.ecommerce.exception.custom.ApiException;
import com.winet.ecommerce.exception.custom.ResourceNotFoundException;
import com.winet.ecommerce.model.Cart;
import com.winet.ecommerce.model.CartItem;
import com.winet.ecommerce.model.Product;
import com.winet.ecommerce.payload.dto.CartDTO;
import com.winet.ecommerce.payload.dto.CartItemDTO;
import com.winet.ecommerce.payload.response.CartResponse;
import com.winet.ecommerce.repository.CartItemRepository;
import com.winet.ecommerce.repository.CartRepository;
import com.winet.ecommerce.repository.ProductRepository;
import com.winet.ecommerce.util.AuthUtils;
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
import java.util.stream.Collectors;

import static com.winet.ecommerce.util.PagingAndSortingUtils.getPageDetails;

@Service
public class CartService {

	private final ProductRepository productRepository;
	private final CartRepository cartRepository;
	private final CartItemRepository cartItemRepository;
	private final DtoUtils dtoUtils;
	private final AuthUtils authUtils;

	@Autowired
	public CartService(ProductRepository productRepository, CartRepository cartRepository, CartItemRepository cartItemRepository, DtoUtils dtoUtils, AuthUtils authUtils) {
		this.productRepository = productRepository;
		this.cartRepository = cartRepository;
		this.cartItemRepository = cartItemRepository;
		this.dtoUtils = dtoUtils;
		this.authUtils = authUtils;
	}

	public CartResponse getAll(Integer page, Integer size, String sort, String order) {
		Pageable pageDetails = getPageDetails(page, size, sort, order);
		return getPaginatedAndSortedProductResponse(
				() -> cartRepository.findAll(pageDetails)
		);
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
		cartItem.setProductPrice(product.getSpecialPrice().equals(BigDecimal.ZERO) ? product.getPrice() : product.getSpecialPrice());
		cartItemRepository.save(cartItem);

		userCart.getCartItems().add(cartItem);
		userCart.setTotalPrice(userCart.getTotalPrice().add(
				cartItem.getProductPrice()
						.multiply(BigDecimal.valueOf(quantity)))
		);
		userCart = cartRepository.save(userCart);

		return dtoUtils.convertCartToDTO(userCart);
	}

	public CartDTO update(CartDTO cartDTO) {

		String email = authUtils.loggedInEmail();

		Cart userCart = cartRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("Cart", "email", email));

		List<Long> updatedProductIds = cartDTO.getCartItems().stream()
				.map(ci -> ci.getProduct().getProductId())
				.toList();

		List<CartItem> itemsToRemove = userCart.getCartItems().stream()
				.filter(item -> !updatedProductIds.contains(item.getProduct().getProductId()))
				.toList();


		if (!itemsToRemove.isEmpty()) {
			itemsToRemove.forEach(item -> cartItemRepository.deleteById(item.getCartItemId()));
			userCart.getCartItems().removeAll(itemsToRemove);
		}

		cartDTO.getCartItems().forEach(ciDTO -> {

			CartItem existingItem = userCart.getCartItems().stream()
					.filter(ci -> ci.getProduct().getProductId().equals(ciDTO.getProduct().getProductId()))
					.findFirst()
					.orElse(null);

			if(existingItem != null) {

				existingItem.setQuantity(ciDTO.getQuantity());
				existingItem.setDiscount(ciDTO.getDiscount());
				existingItem.setProductPrice(ciDTO.getProduct().getSpecialPrice());

			} else {

				Product product = productRepository.findById(ciDTO.getProduct().getProductId())
						.orElseThrow(() -> new ResourceNotFoundException("Product", "productId", ciDTO.getProduct().getProductId()));

				CartItem newItem = new CartItem();
				newItem.setProduct(product);
				newItem.setCart(userCart);
				newItem.setQuantity(ciDTO.getQuantity());
				newItem.setDiscount(ciDTO.getDiscount());
				newItem.setProductPrice(ciDTO.getProduct().getSpecialPrice());

				userCart.getCartItems().add(newItem);

			}
		});

		userCart.setTotalPrice(calculateTotalPrice(userCart));
		Cart updatedCart = cartRepository.save(userCart);

		return dtoUtils.convertCartToDTO(updatedCart);
	}

	@Transactional
	public CartDTO deleteProduct(Long productId) {
		String email = authUtils.loggedInEmail();

		Cart userCart = cartRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("Cart", "email", email));

		CartItem cartItem = cartItemRepository.findByProductIdAndCartId(productId, userCart.getCartId())
				.orElseThrow(() -> new ApiException("Product is not in the cart"));

		cartItemRepository.delete(productId, userCart.getCartId());
		userCart.setTotalPrice(userCart.getTotalPrice().subtract(
				cartItem.getProductPrice()
						.multiply(BigDecimal.valueOf(cartItem.getQuantity()))
		));
		userCart.getCartItems().remove(cartItem);

		return dtoUtils.convertCartToDTO(userCart);
	}

	public CartDTO getForUser() {
		String email = authUtils.loggedInEmail();

		Cart cart = cartRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("Cart", "email", email));
		return dtoUtils.convertCartToDTO(cart);
	}

	private Cart createCart() {
		String email = authUtils.loggedInEmail();

		Cart userCart = cartRepository.findByEmail(email)
				.orElse(null);

		if(userCart != null) {
			return userCart;
		}

		Cart cart = new Cart();
		cart.setTotalPrice(BigDecimal.ZERO);
		cart.setUser(authUtils.loggedInUser());

		return cartRepository.save(cart);
	}

	private BigDecimal calculateTotalPrice(Cart cartItems) {
		BigDecimal totalPrice = BigDecimal.ZERO;
		for(CartItem cartItem : cartItems.getCartItems()) {
			totalPrice = totalPrice.add(cartItem.getProductPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
		}
		return totalPrice;
	}

	private CartResponse getPaginatedAndSortedProductResponse(Supplier<Page<Cart>> query) {
		Page<Cart> productsPage = query.get();
		List<Cart> allCarts = productsPage.getContent();

		if(allCarts.isEmpty()) throw new ApiException("No carts found");

		List<CartDTO> cartDTOs = allCarts.stream()
				.map(dtoUtils::convertCartToDTO)
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

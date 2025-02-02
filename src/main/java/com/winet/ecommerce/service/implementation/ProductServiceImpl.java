package com.winet.ecommerce.service.implementation;

import com.winet.ecommerce.exception.custom.ApiException;
import com.winet.ecommerce.exception.custom.ResourceNotFoundException;
import com.winet.ecommerce.model.Product;
import com.winet.ecommerce.payload.dto.ProductDTO;
import com.winet.ecommerce.payload.response.ProductResponse;
import com.winet.ecommerce.repository.ProductRepository;
import com.winet.ecommerce.service.ProductService;
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
public class ProductServiceImpl implements ProductService {

	private final ProductRepository productRepository;
	private final ModelMapper modelMapper;

	@Autowired
	public ProductServiceImpl(ProductRepository productRepository, ModelMapper modelMapper) {
		this.productRepository = productRepository;
		this.modelMapper = modelMapper;
	}

	@Override
	public ProductResponse getAllProducts(Integer page, Integer size, String sort, String order) {
		Pageable pageDetails = getPageDetails(page, size, sort, order);
		return getPaginatedAndSortedProductResponse(() -> productRepository.findAll(pageDetails));
	}

	@Override
	public ProductDTO getProduct(Long id) {
		Product product = productRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Product", "productId", "id"));

		return modelMapper.map(product, ProductDTO.class);
	}

	@Override
	public ProductDTO addProduct(ProductDTO productDTO) {

		if(productRepository.existsByProductName(productDTO.getProductName())) {
			throw new ApiException("Product with this name already exists");
		}

		Product product = modelMapper.map(productDTO, Product.class);

		product.setImage("default.png");

		BigDecimal specialPrice = getDiscountedPrice(product.getPrice(), product.getDiscount());
		product.setSpecialPrice(specialPrice);

		Product savedProduct = productRepository.save(product);

		return modelMapper.map(savedProduct, ProductDTO.class);
	}

	@Override
	public ProductDTO updateProduct(Long id, ProductDTO productDTO) {

		Product product = productRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Product", "productId", id));

		product.setProductName(productDTO.getProductName());
		product.setDescription(productDTO.getDescription());
		product.setPrice(productDTO.getPrice());
		product.setDiscount(productDTO.getDiscount());
		product.setSpecialPrice(getDiscountedPrice(productDTO.getPrice(), productDTO.getDiscount()));

		Product savedProduct = productRepository.save(product);

		return modelMapper.map(savedProduct, ProductDTO.class);
	}

	@Override
	public ProductDTO deleteProduct(Long productId) {
		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

		productRepository.deleteById(productId);

		return modelMapper.map(product, ProductDTO.class);
	}

	@Override
	public ProductResponse searchProductsByKeyword(String keyword, Integer page, Integer size, String sort, String order) {
		Pageable pageDetails = getPageDetails(page, size, sort, order);
		return getPaginatedAndSortedProductResponse(() -> productRepository.findByProductNameContainsIgnoreCase(keyword, pageDetails));
	}

	@Override
	public ProductResponse searchProductsByPrice(Double minPrice, Double maxPrice, Integer page, Integer size, String sort, String order) {
		Pageable pageDetails = getPageDetails(page, size, sort, order);
		return getPaginatedAndSortedProductResponse(() -> productRepository.findByPriceBetween(BigDecimal.valueOf(minPrice), BigDecimal.valueOf(maxPrice), pageDetails));
	}

	private ProductResponse getPaginatedAndSortedProductResponse(Supplier<Page<Product>> query) {
		Page<Product> productsPage = query.get();
		List<Product> allProducts = productsPage.getContent();

		if(allProducts.isEmpty()) throw new ApiException("No products found");

		List<ProductDTO> productDTOs = allProducts.stream()
				.map(product -> modelMapper.map(product, ProductDTO.class))
				.toList();

		return new ProductResponse(
				productDTOs,
				productsPage.getNumber(),
				productsPage.getSize(),
				productsPage.getTotalElements(),
				productsPage.getTotalPages(),
				productsPage.isLast()
		);
	}

	private BigDecimal getDiscountedPrice(BigDecimal price, Double discount) {
		return discount != null
				? price.subtract(BigDecimal.valueOf(discount * 0.01).multiply(price))
				: BigDecimal.ZERO;
	}

}

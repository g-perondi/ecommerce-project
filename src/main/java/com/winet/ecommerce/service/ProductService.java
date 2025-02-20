package com.winet.ecommerce.service;

import com.winet.ecommerce.exception.custom.ApiException;
import com.winet.ecommerce.exception.custom.ResourceNotFoundException;
import com.winet.ecommerce.model.Product;
import com.winet.ecommerce.payload.dto.ProductDTO;
import com.winet.ecommerce.payload.response.ProductResponse;
import com.winet.ecommerce.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.function.Supplier;

import static com.winet.ecommerce.util.PagingAndSortingUtils.getPageDetails;


@Service
public class ProductService {

	private final Logger LOGGER = LoggerFactory.getLogger(ProductService.class);

	private final ProductRepository productRepository;
	private final FileService fileService;
	private final ModelMapper modelMapper;

	@Autowired
	public ProductService(ProductRepository productRepository, FileService fileService, ModelMapper modelMapper) {
		this.productRepository = productRepository;
		this.fileService = fileService;
		this.modelMapper = modelMapper;
	}

	public ProductResponse getAll(Integer page, Integer size, String sort, String order) {
		Pageable pageDetails = getPageDetails(page, size, sort, order);
		return getPaginatedAndSortedProductResponse(
				() -> productRepository.findAll(pageDetails)
		);
	}

	public ProductDTO get(Long id) {
		Product product = productRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Product", "productId", "id"));

		return modelMapper.map(product, ProductDTO.class);
	}

	public ProductDTO add(ProductDTO productDTO) {

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

	public ProductDTO update(Long id, ProductDTO productDTO) {

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

	public ProductDTO delete(Long productId) {
		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

		if(product.getImage() != null && !product.getImage().equals("default.png")) {
			try {
				fileService.deleteImage(product.getImage());
			} catch(IOException e) {
				LOGGER.error("Image could not be deleted");
			}
		}

		productRepository.deleteById(productId);

		return modelMapper.map(product, ProductDTO.class);
	}

	public ProductResponse search(String keyword, Double minPrice, Double maxPrice, Integer page, Integer size, String sort, String order) {
		Pageable pageDetails = getPageDetails(page, size, sort, order);
		return getPaginatedAndSortedProductResponse(
				() -> productRepository.findByProductNameContainsIgnoreCaseAndSpecialPriceBetween(keyword, BigDecimal.valueOf(minPrice), BigDecimal.valueOf(maxPrice), pageDetails)
		);
	}

	@Transactional
	public ProductDTO updateImage(Long productId, MultipartFile image) {
		Product productFromDb = productRepository.findById(productId)
				.orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

		if(productFromDb.getImage() != null && !productFromDb.getImage().equals("default.png")) {
			try {
				fileService.deleteImage(productFromDb.getImage());
			} catch(IOException e) {
				throw new ApiException("Previous file deletion failed");
			}
		}

		String filename;

		try {
			filename = fileService.uploadImage(image);
		} catch(IOException e) {
			throw new ApiException("Error while uploading image");
		}

		productFromDb.setImage(filename);
		Product updatedProduct = productRepository.save(productFromDb);

		return modelMapper.map(updatedProduct, ProductDTO.class);
	}

	@Transactional
	public void importCsv(MultipartFile file) {

		List<ProductDTO> fromCsv;

		try {
			fromCsv = this.fileService.readProductsCsv(file);
		} catch(IOException e) {
			throw new ApiException("Error while reading products from CSV");
		}

		List<Product> productsList = fromCsv.stream()
				.map(productDTO -> modelMapper.map(productDTO, Product.class))
				.toList();

		for(Product product : productsList) {
			if(productRepository.existsByProductName(product.getProductName())) {
				LOGGER.error("Product with name {} this name already exists", product.getProductName());
			} else {
				product.setProductId(null);
				Product imported = productRepository.save(product);
				LOGGER.info("Imported product: {}", imported.getProductName());
			}
		}
	}

	@Transactional
	public InputStreamResource exportCsv() {
		List<Product> allProducts = productRepository.findAll();

		List<ProductDTO> allProductsDTO = allProducts.stream()
				.map(product -> modelMapper.map(product, ProductDTO.class))
				.toList();

		if(allProducts.isEmpty()) {
			throw new ApiException("No products found");
		}

		InputStreamResource csv;

		try {
			csv = fileService.generateProductsCsv(allProductsDTO);
		} catch(IOException e) {
			throw new ApiException("Error while exporting products to CSV");
		}

		return csv;
	}

	/**
	 * Retrieves a paginated and sorted list of products using the provided query supplier.
	 * The query should return a {@link Page}<{@link Product}>, which is then converted into a
	 * {@link ProductResponse} containing a list of {@link ProductDTO} along with pagination properties.
	 *
	 * @param query a {@link Supplier} that provides a {@link Page}<{@link Product}> when invoked.
	 * @return a {@link ProductResponse} containing the paginated product data.
	 */
	private ProductResponse getPaginatedAndSortedProductResponse(Supplier<Page<Product>> query) {
		Page<Product> productsPage = query.get();
		List<Product> allProducts = productsPage.getContent();

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

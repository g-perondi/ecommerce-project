package com.winet.ecommerce.service;

import com.winet.ecommerce.model.Product;
import com.winet.ecommerce.payload.dto.ProductDTO;
import com.winet.ecommerce.payload.response.ProductResponse;
import com.winet.ecommerce.repository.ProductRepository;
import com.winet.ecommerce.service.implementation.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

	@Mock(strictness = Mock.Strictness.LENIENT)
	private ModelMapper modelMapper;

	@Mock
	private ProductRepository productRepository;

	@Mock
	private FileService fileService;

	@InjectMocks
	private ProductServiceImpl productService;

	private Product product;
	private ProductDTO productDTO;

	@BeforeEach
	void setUp() {
		product = new Product(1L, "Laptop", "description", new BigDecimal("1200.00"), BigDecimal.ZERO, 0.0, "laptop.jpg");
		productDTO = new ProductDTO(1L, "Laptop", "description", new BigDecimal("1200.00"), BigDecimal.ZERO, 0.0, "laptop.png");
		given(modelMapper.map(product, ProductDTO.class)).willReturn(productDTO);
		given(modelMapper.map(productDTO, Product.class)).willReturn(product);
	}

	@Test
	void testGetProductById_BasicScenario() {
		given(productRepository.findById(1L)).willReturn(Optional.of(product));

		ProductDTO result = productService.getProduct(1L);

		assertThat(result).isNotNull();
		assertThat(result.getProductName()).isEqualTo("Laptop");
	}

	@Test
	void testAddProduct_BasicScenario() {
		given(productRepository.save(product)).willReturn(product);

		ProductDTO result = productService.addProduct(productDTO);

		assertThat(result).isNotNull();
		assertThat(result.getProductName()).isEqualTo("Laptop");
	}

	@Test
	void testDeleteProduct_BasicScenario() {
		given(productRepository.findById(1L)).willReturn(Optional.of(product));
		willDoNothing().given(productRepository).deleteById(1L);

		ProductDTO result = productService.deleteProduct(1L);

		assertThat(result).isNotNull();
		assertThat(result.getProductName()).isEqualTo("Laptop");
	}

	@Test
	void testUpdateProductImage_BasicScenario() throws IOException {
		MultipartFile mockImage = mock(MultipartFile.class);

		given(productRepository.findById(1L)).willReturn(Optional.of(product));
		given(fileService.uploadImage(mockImage)).willReturn(product.getImage());
		given(productRepository.save(any())).willReturn(product);

		ProductDTO result = productService.updateProductImage(1L, mockImage);

		assertThat(result).isNotNull();
		assertThat(result.getProductName()).isEqualTo("Laptop");
		assertThat(result.getImage()).isEqualTo("laptop.png");
	}

	@Test
	void testSearchProductsByKeyword_BasicScenario() {
		Pageable pageable = PageRequest.of(0, 10, Sort.by("productName").ascending());
		Page<Product> page = new PageImpl<>(List.of(product));

		given(productRepository.findByProductNameContainsIgnoreCase("Laptop", pageable))
				.willReturn(page);

		ProductResponse response = productService.searchProductsByKeyword("Laptop", 0, 10, "productName", "asc");

		assertThat(response.getTotalElements()).isEqualTo(1);
		assertThat(response.getContent().get(0).getProductName()).isEqualTo("Laptop");
	}

	@Test
	void testSearchProductsByPrice_BasicScenario() {
		Pageable pageable = PageRequest.of(0, 10, Sort.by("productName").ascending());
		Page<Product> page = new PageImpl<>(List.of(product));

		given(productRepository.findByPriceBetween(BigDecimal.valueOf(1000.0), BigDecimal.valueOf(1500.0), pageable))
				.willReturn(page);
		given(modelMapper.map(any(Product.class), eq(ProductDTO.class))).willReturn(productDTO);

		ProductResponse response = productService.searchProductsByPrice(1000.0, 1500.0, 0, 10, "productName", "asc");

		assertThat(response.getTotalElements()).isEqualTo(1);
		assertThat(response.getContent().size()).isEqualTo(1);
		assertThat(response.getContent().get(0).getProductName()).isEqualTo("Laptop");
	}

	@Test
	void testImportAllProductsFromCsv() throws IOException {
		MultipartFile mockCsv = mock(MultipartFile.class);

		List<Product> parsedProducts = List.of(product);
		List<ProductDTO> parsedProductsDTO = List.of(productDTO);

		given(fileService.readProductsCsv(mockCsv)).willReturn(parsedProductsDTO);

		productService.importAllProductsFromCsv(mockCsv);

		then(fileService).should(times(1)).readProductsCsv(mockCsv);
		then(productRepository).should(times(1)).saveAll(parsedProducts);
	}

	@Test
	void testExportAllProductsToCsv() throws IOException {

		List<Product> products = List.of(product);
		List<ProductDTO> productsDTO = List.of(productDTO);
		String csvData = "id,productName,description,price,discount,rating,imageUrl\n1,Laptop,description,1200.00,0.00,0.0,laptop.jpg\n";
		InputStreamResource inputStreamResource = new InputStreamResource(new ByteArrayInputStream(csvData.getBytes()));

		given(productRepository.findAll()).willReturn(products);

		given(fileService.generateProductsCsv(productsDTO)).willReturn(inputStreamResource);

		InputStreamResource result = productService.exportAllProductsToCsv();

		assertThat(result).isNotNull();
		then(fileService).should(times(1)).generateProductsCsv(productsDTO);
	}


}

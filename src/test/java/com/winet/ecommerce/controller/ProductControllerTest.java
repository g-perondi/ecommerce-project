package com.winet.ecommerce.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.winet.ecommerce.payload.dto.ProductDTO;
import com.winet.ecommerce.payload.response.ProductResponse;
import com.winet.ecommerce.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.List;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ProductController.class)
@ExtendWith(MockitoExtension.class)
public class ProductControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ProductService productService;

	private ProductDTO productDTO;
	private ProductResponse productResponse;

	@BeforeEach
	void setUp() {
		productDTO = new ProductDTO(1L, "Laptop", "description", new BigDecimal("1200.00"), BigDecimal.ZERO, 0.0, "laptop.jpg");
		productResponse = new ProductResponse(List.of(productDTO), 0, 20, 1L, 1, true);
	}

	@Test
	void testGetAll() throws Exception {
		given(productService.getAll(0, 10, "name", "asc")).willReturn(productResponse);

		mockMvc.perform(get("/api/v1/public/products")
						.param("page", "0")
						.param("size", "10")
						.param("sort", "name")
						.param("order", "asc")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalElements").value(1))
				.andExpect(jsonPath("$.content[0].productName").value("Laptop"));
	}

	@Test
	void testGet() throws Exception {
		given(productService.get(1L)).willReturn(productDTO);

		mockMvc.perform(get("/api/v1/public/products/1")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.productName").value("Laptop"));
	}

	@Test
	void testAdd() throws Exception {
		given(productService.add(any(ProductDTO.class))).willReturn(productDTO);

		mockMvc.perform(post("/api/v1/admin/products")
						.contentType(MediaType.APPLICATION_JSON)
						.content(new ObjectMapper().writeValueAsString(productDTO)))
				.andExpect(status().isCreated())
				.andExpect(header().exists("Location"))
				.andExpect(jsonPath("$.productName").value("Laptop"));
	}

	@Test
	void testUpdate() throws Exception {
		given(productService.update(eq(1L), any(ProductDTO.class))).willReturn(productDTO);

		mockMvc.perform(put("/api/v1/admin/products/1")
						.contentType(MediaType.APPLICATION_JSON)
						.content(new ObjectMapper().writeValueAsString(productDTO)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.productName").value("Laptop"));
	}

	@Test
	void testDelete() throws Exception {
		given(productService.delete(1L)).willReturn(productDTO);

		mockMvc.perform(delete("/api/v1/admin/products/1")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.productName").value("Laptop"));
	}


	@Test
	void testSearch_ByName() throws Exception {
		// Given
		given(productService.search("Laptop", 0, 10, "name", "asc")).willReturn(productResponse);

		// When & Then
		mockMvc.perform(get("/api/v1/public/products/search")
						.param("keyword", "Laptop")
						.param("page", "0")
						.param("size", "10")
						.param("sort", "name")
						.param("order", "asc")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].productName").value("Laptop"));
	}

	@Test
	void testSearch_ByPrice() throws Exception {
		// Given
		given(productService.search(1000.0, 1500.0, 0, 10, "price", "asc")).willReturn(productResponse);

		// When & Then
		mockMvc.perform(get("/api/v1/public/products/price")
						.param("min", "1000.0")
						.param("max", "1500.0")
						.param("page", "0")
						.param("size", "10")
						.param("sort", "price")
						.param("order", "asc")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].productName").value("Laptop"));
	}

	@Test
	void testUpdateImage() throws Exception {
		// Given
		MockMultipartFile imageFile = new MockMultipartFile("image", "laptop.jpg", "image/jpeg", "fake-image-content".getBytes());
		given(productService.updateImage(eq(1L), any(MultipartFile.class))).willReturn(productDTO);

		// When & Then
		mockMvc.perform(
						multipart(HttpMethod.PUT, "/api/v1/admin/products/1/image").file(imageFile)
				)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.productName").value("Laptop"));
	}

	@Test
	void testExportCsv() throws Exception {
		// Given
		InputStreamResource csvResource = new InputStreamResource(new ByteArrayInputStream("id,productName\n1,Laptop".getBytes()));
		given(productService.exportCsv()).willReturn(csvResource);

		// When & Then
		mockMvc.perform(get("/api/v1/admin/products/export-csv"))
				.andExpect(status().isOk())
				.andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=products.csv"));
	}

	@Test
	void testImportCsv() throws Exception {
		MockMultipartFile csvFile = new MockMultipartFile(
				"products",
				"products.csv",
				"text/csv",
				"id,productName,description,price,discount,rating,imageUrl\n1,Laptop,description,1200.00,0.00,0.0,laptop.jpg".getBytes()
		);

		mockMvc.perform(multipart("/api/v1/admin/products/import-csv")
						.file(csvFile)
						.contentType("text/csv")
				)
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.message").value("Product import successful."));
	}


}

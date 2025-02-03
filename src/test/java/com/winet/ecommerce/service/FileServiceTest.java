package com.winet.ecommerce.service;

import com.winet.ecommerce.payload.dto.ProductDTO;
import com.winet.ecommerce.service.implementation.FileServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.InputStreamResource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(MockitoExtension.class)
public class FileServiceTest {

	@TempDir
	Path tempDir;

	@InjectMocks
	private FileServiceImpl fileService;

	@BeforeEach
	void setUp() {
		fileService = new FileServiceImpl();
		fileService.setImagesPath(tempDir.toString());
	}

	@Test
	void testUploadImage() throws IOException {
		MockMultipartFile mockImage = new MockMultipartFile("image", "test.png", "image/png", new byte[]{ 1, 2, 3 });
		String uploadedFileName = fileService.uploadImage(mockImage);

		Path uploadedFilePath = tempDir.resolve(uploadedFileName);

		assertThat(Files.exists(uploadedFilePath)).isTrue();
		assertThat(uploadedFileName).contains(".png");
	}

	@Test
	void testDeleteImage() throws IOException {
		String fileName = UUID.randomUUID() + ".png";
		Path filePath = tempDir.resolve(fileName);
		Files.createFile(filePath);

		fileService.deleteImage(fileName);

		assertThat(Files.exists(filePath)).isFalse();
	}

	@Test
	void testGenerateProductsCsv() throws IOException {
		ProductDTO productDTO = new ProductDTO(1L, "Laptop", "description", new BigDecimal("1200.00"),
				BigDecimal.ZERO, 0.0, "laptop.jpg");
		List<ProductDTO> products = List.of(productDTO);

		InputStreamResource csvResource = fileService.generateProductsCsv(products);

		assertThat(csvResource).isNotNull();
		assertThat(csvResource.getInputStream().available()).isGreaterThan(0);
	}

	@Test
	void testReadProductsCsv() throws IOException {
		String csvContent = """
				productId,productName,description,price,specialPrice,discount,image
				1,Laptop,description,1200.00,0.00,0.0,laptop.png
				""";
		MockMultipartFile csvFile = new MockMultipartFile("file", "products.csv", "text/csv", csvContent.getBytes());

		List<ProductDTO> products = fileService.readProductsCsv(csvFile);

		assertThat(products.isEmpty()).isFalse();
		assertThat(products.get(0).getProductId()).isEqualTo(1L);
		assertThat(products.get(0).getProductName()).isEqualTo("Laptop");
	}

}

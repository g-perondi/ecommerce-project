package com.winet.ecommerce.service;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.winet.ecommerce.model.Product;
import com.winet.ecommerce.payload.dto.ProductDTO;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@Setter
@Getter
public class FileService {

	@Value("${project.images.path}")
	private String imagesPath;

	public String uploadImage(MultipartFile image) throws IOException {

		String originalFileName = image.getOriginalFilename();
		String randomId = UUID.randomUUID().toString();

		if(originalFileName != null) {
			String fileName = randomId + originalFileName.substring(originalFileName.lastIndexOf("."));
			String filePath = imagesPath + File.separator + fileName;

			File directory = new File(imagesPath);

			if(!directory.exists()) {
				directory.mkdirs();
			}

			Files.copy(image.getInputStream(), Paths.get(filePath));
			return fileName;
		}

		throw new IOException("Error parsing image file");
	}

	public void deleteImage(String filename) throws IOException {
		String filepath = imagesPath + File.separator + filename;
		File file = new File(filepath);

		if(file.exists()) {
			file.delete();
			return;
		}

		throw new IOException("Error processing image file");
	}

	public InputStreamResource generateProductsCsv(List<ProductDTO> products) throws IOException {
		CsvMapper mapper = new CsvMapper();
		CsvSchema schema = mapper.schemaFor(ProductDTO.class)
				.withHeader()
				.withColumnReordering(true);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		mapper.writer(schema).writeValue(outputStream, products);

		return new InputStreamResource(
				new ByteArrayInputStream(outputStream.toByteArray())
		);
	}

	public List<ProductDTO> readProductsCsv(MultipartFile csvFile) throws IOException {
		CsvMapper mapper = new CsvMapper();
		CsvSchema schema = mapper.schemaFor(ProductDTO.class)
				.withHeader()
				.withColumnReordering(true);

		try(MappingIterator<ProductDTO> iterator =
					mapper.readerFor(ProductDTO.class).with(schema).readValues(csvFile.getInputStream())) {
			return iterator.readAll();
		}
	}

}

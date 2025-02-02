package com.winet.ecommerce.service.implementation;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.winet.ecommerce.model.Product;
import com.winet.ecommerce.service.FileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

	@Value("${project.images.path}")
	private String imagesPath;

	@Override
	public String uploadImage(MultipartFile image) throws IOException {

		String originalFileName = image.getOriginalFilename();
		String randomId = UUID.randomUUID().toString();

		if(originalFileName != null) {
			String fileName = randomId + originalFileName.substring(originalFileName.lastIndexOf("."));
			String filePath = imagesPath + File.separator + fileName;

			File directory = new File(imagesPath);

			if(!directory.exists()) {
				directory.mkdir();
			}

			Files.copy(image.getInputStream(), Paths.get(filePath));
			return fileName;
		}

		throw new IOException("Error parsing image file");
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	@Override
	public void deleteImage(String filename) throws IOException {
		String filepath = imagesPath + File.separator + filename;
		File file = new File(filepath);

		if(file.exists()) {
			file.delete();
			return;
		}

		throw new IOException("Error processing image file");
	}

	public InputStreamResource generateProductsCsv(List<Product> products) throws IOException {
		CsvMapper mapper = new CsvMapper();
		CsvSchema schema = mapper.schemaFor(Product.class).withHeader();

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		mapper.writer(schema).writeValue(outputStream, products);

		return new InputStreamResource(
				new ByteArrayInputStream(outputStream.toByteArray())
		);
	}

}

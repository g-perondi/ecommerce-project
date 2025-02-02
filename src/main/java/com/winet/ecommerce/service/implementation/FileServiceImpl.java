package com.winet.ecommerce.service.implementation;

import com.winet.ecommerce.service.FileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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

}

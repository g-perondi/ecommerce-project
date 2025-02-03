package com.winet.ecommerce.service;

import com.winet.ecommerce.payload.dto.ProductDTO;
import org.springframework.core.io.InputStreamResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface FileService {

	String uploadImage(MultipartFile image) throws IOException;

	void deleteImage(String filename) throws IOException;

	InputStreamResource generateProductsCsv(List<ProductDTO> products) throws IOException;

	public List<ProductDTO> readProductsCsv(MultipartFile csvFile) throws IOException;

}

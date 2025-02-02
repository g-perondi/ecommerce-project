package com.winet.ecommerce.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileService {

	String uploadImage(MultipartFile image) throws IOException;

	void deleteImage(String filename) throws IOException;

}

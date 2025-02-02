package com.winet.ecommerce.service;

import com.winet.ecommerce.payload.dto.ProductDTO;
import com.winet.ecommerce.payload.response.ProductResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ProductService {

	ProductResponse getAllProducts(Integer page, Integer size, String sort, String order);

	ProductDTO getProduct(Long id);

	ProductDTO addProduct(ProductDTO product);

	ProductDTO updateProduct(Long id, ProductDTO product);

	ProductDTO deleteProduct(Long productId);

	ProductResponse searchProductsByKeyword(String keyword, Integer page, Integer size, String sort, String order);

	ProductResponse searchProductsByPrice(Double minPrice, Double maxPrice, Integer page, Integer size, String sort, String order);

	ProductDTO updateProductImage(Long productId, MultipartFile image);

}

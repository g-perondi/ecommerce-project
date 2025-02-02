package com.winet.ecommerce.controller;

import com.winet.ecommerce.payload.dto.ProductDTO;
import com.winet.ecommerce.payload.response.ProductResponse;
import com.winet.ecommerce.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

import static com.winet.ecommerce.util.PagingAndSortingUtils.*;

@RestController
@RequestMapping("/api/v1")
public class ProductController {

	private final ProductService productService;

	@Autowired
	public ProductController(ProductService productService) {
		this.productService = productService;
	}

	@GetMapping("public/products")
	public ResponseEntity<ProductResponse> getProducts(
			@RequestParam(name = "page", defaultValue = PAGE_NUMBER, required = false) int page,
			@RequestParam(name = "size", defaultValue = PAGE_SIZE, required = false) int size,
			@RequestParam(name = "sort", defaultValue = PRODUCT_DEFAULT_SORT_BY, required = false) String sort,
			@RequestParam(name = "order", defaultValue = DEFAULT_ORDER_BY, required = false) String order
	) {
		ProductResponse allProducts = productService.getAllProducts(page, size, sort, order);
		return new ResponseEntity<>(allProducts, HttpStatus.OK);
	}

	@GetMapping("/public/products/{productId}")
	public ResponseEntity<ProductDTO> getProduct(@PathVariable("productId") long productId) {
		ProductDTO product = productService.getProduct(productId);
		return new ResponseEntity<>(product, HttpStatus.OK);
	}

	@PostMapping("/admin/products")
	public ResponseEntity<ProductDTO> addProduct(@Valid @RequestBody ProductDTO product) {
		ProductDTO savedProduct = productService.addProduct(product);

		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{productId}")
				.buildAndExpand(savedProduct.getProductId())
				.toUri();

		return ResponseEntity.created(location).body(product);
	}

	@PutMapping("/admin/products/{productId}")
	public ResponseEntity<ProductDTO> updateProduct(@PathVariable("productId") long productId, @Valid @RequestBody ProductDTO product) {
		ProductDTO savedProduct = productService.updateProduct(productId, product);
		return new ResponseEntity<>(savedProduct, HttpStatus.OK);
	}

	@DeleteMapping("/admin/products/{productId}")
	public ResponseEntity<ProductDTO> deleteProduct(@PathVariable("productId") long productId) {
		ProductDTO deletedProduct = productService.deleteProduct(productId);
		return new ResponseEntity<>(deletedProduct, HttpStatus.OK);
	}

	@GetMapping(value = "/public/products/search")
	public ResponseEntity<ProductResponse> getAllProductsByKeyword(
			@RequestParam(name = "keyword") String keyword,
			@RequestParam(name = "page", defaultValue = PAGE_NUMBER, required = false) int page,
			@RequestParam(name = "size", defaultValue = PAGE_SIZE, required = false) int size,
			@RequestParam(name = "sort", defaultValue = PRODUCT_DEFAULT_SORT_BY, required = false) String sort,
			@RequestParam(name = "order", defaultValue = DEFAULT_ORDER_BY, required = false) String order
	) {
		ProductResponse allProductsByKeyword = productService.searchProductsByKeyword(keyword, page, size, sort, order);
		return new ResponseEntity<>(allProductsByKeyword, HttpStatus.OK);
	}

	@GetMapping("/public/products/price")
	public ResponseEntity<ProductResponse> getAllProductsByPrice(
			@RequestParam(name = "min", defaultValue = PRODUCT_DEFAULT_MIN_PRICE, required = false) double minPrice,
			@RequestParam(name = "max", defaultValue = PRODUCT_DEFAULT_MAX_PRICE, required = false) double maxPrice,
			@RequestParam(name = "page", defaultValue = PAGE_NUMBER, required = false) int page,
			@RequestParam(name = "size", defaultValue = PAGE_SIZE, required = false) int size,
			@RequestParam(name = "sort", defaultValue = PRODUCT_DEFAULT_SORT_BY, required = false) String sort,
			@RequestParam(name = "order", defaultValue = DEFAULT_ORDER_BY, required = false) String order
	) {
		ProductResponse allProductsByPrice = productService.searchProductsByPrice(minPrice, maxPrice, page, size, sort, order);
		return new ResponseEntity<>(allProductsByPrice, HttpStatus.OK);
	}

	@PutMapping("/admin/products/{productId}/image")
	public ResponseEntity<ProductDTO> updateProductImage(@PathVariable Long productId, @RequestPart("image") MultipartFile image) {
		ProductDTO updatedProduct = productService.updateProductImage(productId, image);
		return new ResponseEntity<>(updatedProduct, HttpStatus.OK);
	}

	@GetMapping(value = "/admin/products/export-csv", produces = "text/csv")
	public ResponseEntity<InputStreamResource> getAllProductsCsv() {
		InputStreamResource csv = this.productService.exportAllProductsToCsv();

		String csvFileName = "products.csv";
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + csvFileName);
		headers.add(HttpHeaders.CONTENT_TYPE, "text/csv");

		return new ResponseEntity<>(
				csv,
				headers,
				HttpStatus.OK);
	}

}

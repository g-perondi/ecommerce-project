package com.winet.ecommerce.controller;

import com.winet.ecommerce.payload.dto.ProductDTO;
import com.winet.ecommerce.payload.response.ApiResponse;
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
@CrossOrigin(origins = "*")
public class ProductController {

	private final ProductService productService;

	@Autowired
	public ProductController(ProductService productService) {
		this.productService = productService;
	}

	@GetMapping("public/products")
	public ResponseEntity<ProductResponse> get(
			@RequestParam(name = "page", defaultValue = PAGE_NUMBER, required = false) int page,
			@RequestParam(name = "size", defaultValue = PAGE_SIZE, required = false) int size,
			@RequestParam(name = "sort", defaultValue = PRODUCT_DEFAULT_SORT_BY, required = false) String sort,
			@RequestParam(name = "order", defaultValue = DEFAULT_ORDER_BY, required = false) String order
	) {
		ProductResponse allProducts = productService.getAll(page, size, sort, order);
		return new ResponseEntity<>(allProducts, HttpStatus.OK);
	}

	@GetMapping("/public/products/{productId}")
	public ResponseEntity<ProductDTO> get(@PathVariable("productId") long productId) {
		ProductDTO product = productService.get(productId);
		return new ResponseEntity<>(product, HttpStatus.OK);
	}

	@GetMapping(value = "/public/products/search")
	public ResponseEntity<ProductResponse> search(
			@RequestParam(name = "keyword", required = false, defaultValue = "") String keyword,
			@RequestParam(name = "min", defaultValue = PRODUCT_DEFAULT_MIN_PRICE, required = false) double minPrice,
			@RequestParam(name = "max", defaultValue = PRODUCT_DEFAULT_MAX_PRICE, required = false) double maxPrice,
			@RequestParam(name = "page", defaultValue = PAGE_NUMBER, required = false) int page,
			@RequestParam(name = "size", defaultValue = PAGE_SIZE, required = false) int size,
			@RequestParam(name = "sort", defaultValue = PRODUCT_DEFAULT_SORT_BY, required = false) String sort,
			@RequestParam(name = "order", defaultValue = DEFAULT_ORDER_BY, required = false) String order
	) {
		ProductResponse allProductsByKeyword = productService.search(keyword, minPrice, maxPrice, page, size, sort, order);
		return new ResponseEntity<>(allProductsByKeyword, HttpStatus.OK);
	}

	@PostMapping("/admin/products")
	public ResponseEntity<ProductDTO> add(@Valid @RequestBody ProductDTO product) {
		ProductDTO savedProduct = productService.add(product);

		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{productId}")
				.buildAndExpand(savedProduct.getProductId())
				.toUri();

		return ResponseEntity.created(location).body(savedProduct);
	}

	@PutMapping("/admin/products/{productId}")
	public ResponseEntity<ProductDTO> update(@PathVariable("productId") long productId, @Valid @RequestBody ProductDTO product) {
		ProductDTO savedProduct = productService.update(productId, product);
		return new ResponseEntity<>(savedProduct, HttpStatus.OK);
	}

	@DeleteMapping("/admin/products/{productId}")
	public ResponseEntity<ProductDTO> delete(@PathVariable("productId") long productId) {
		ProductDTO deletedProduct = productService.delete(productId);
		return new ResponseEntity<>(deletedProduct, HttpStatus.OK);
	}

	@PutMapping("/admin/products/{productId}/image")
	public ResponseEntity<ProductDTO> updateImage(@PathVariable Long productId, @RequestPart("image") MultipartFile image) {
		ProductDTO updatedProduct = productService.updateImage(productId, image);
		return new ResponseEntity<>(updatedProduct, HttpStatus.OK);
	}

	@GetMapping(value = "/admin/products/export-csv", produces = "text/csv")
	public ResponseEntity<InputStreamResource> getCsv() {
		InputStreamResource csv = this.productService.exportCsv();

		String csvFileName = "products.csv";
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + csvFileName);
		headers.add(HttpHeaders.CONTENT_TYPE, "text/csv");

		return new ResponseEntity<>(
				csv,
				headers,
				HttpStatus.OK
		);
	}

	@PostMapping(value = "/admin/products/import-csv")
	public ResponseEntity<ApiResponse> importCsv(@RequestPart("products") MultipartFile products) {
		this.productService.importCsv(products);
		return new ResponseEntity<>(new ApiResponse("Product import completed.", true), HttpStatus.CREATED);
	}

}

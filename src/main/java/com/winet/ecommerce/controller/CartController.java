package com.winet.ecommerce.controller;

import com.winet.ecommerce.payload.response.CartResponse;
import com.winet.ecommerce.service.CartService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.winet.ecommerce.util.PagingAndSortingUtils.*;

@RestController
@RequestMapping("api/v1")
public class CartController {

	private final CartService cartService;

	public CartController(CartService cartService) {
		this.cartService = cartService;
	}

	@GetMapping("admin/carts")
	public ResponseEntity<CartResponse> getAll(
			@RequestParam(name = "page", defaultValue = PAGE_NUMBER, required = false) int page,
			@RequestParam(name = "size", defaultValue = PAGE_SIZE, required = false) int size,
			@RequestParam(name = "sort", defaultValue = CART_DEFAULT_SORT_BY, required = false) String sort,
			@RequestParam(name = "order", defaultValue = DEFAULT_ORDER_BY, required = false) String order
	) {
		CartResponse cartResponse = this.cartService.getAll(page, size, sort, order);
		return new ResponseEntity<>(cartResponse, HttpStatus.OK);
	}

}

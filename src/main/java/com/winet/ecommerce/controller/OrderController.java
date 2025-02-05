package com.winet.ecommerce.controller;

import com.winet.ecommerce.payload.dto.OrderDTO;
import com.winet.ecommerce.payload.dto.OrderRequest;
import com.winet.ecommerce.service.OrderService;
import com.winet.ecommerce.util.AuthUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class OrderController {

	private final OrderService orderService;

	@Autowired
	public OrderController(OrderService orderService) {
		this.orderService = orderService;
	}

	@PostMapping("order/place")
	public ResponseEntity<OrderDTO> place(@RequestBody OrderRequest orderRequest) {
		OrderDTO order = orderService.place(orderRequest);
		return new ResponseEntity<>(order, HttpStatus.CREATED);
	}

}

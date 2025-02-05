package com.winet.ecommerce.service;

import com.winet.ecommerce.payload.dto.OrderDTO;
import com.winet.ecommerce.repository.CartRepository;
import com.winet.ecommerce.repository.OrderItemRepository;
import com.winet.ecommerce.repository.OrderRepository;
import com.winet.ecommerce.repository.PaymentRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

	private final OrderRepository orderRepository;
	private final OrderItemRepository orderItemRepository;
	private final PaymentRepository paymentRepository;
	private final CartRepository cartRepository;
	private final CartService cartService;
	private final ModelMapper modelMapper;

	@Autowired
	public OrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository, PaymentRepository paymentRepository, CartRepository cartRepository, CartService cartService, ModelMapper modelMapper) {
		this.orderRepository = orderRepository;
		this.orderItemRepository = orderItemRepository;
		this.paymentRepository = paymentRepository;
		this.cartRepository = cartRepository;
		this.cartService = cartService;
		this.modelMapper = modelMapper;
	}

	public OrderDTO place() {
		return null;
	}

}

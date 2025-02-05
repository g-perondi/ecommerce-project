package com.winet.ecommerce.service;

import com.winet.ecommerce.exception.custom.ApiException;
import com.winet.ecommerce.exception.custom.ResourceNotFoundException;
import com.winet.ecommerce.model.*;
import com.winet.ecommerce.payload.dto.*;
import com.winet.ecommerce.repository.*;
import com.winet.ecommerce.util.AuthUtils;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

	private final AuthUtils authUtils;
	private final OrderRepository orderRepository;
	private final CartRepository cartRepository;
	private final ModelMapper modelMapper;
	private final PaymentRepository paymentRepository;
	private final CartItemRepository cartItemRepository;

	@Autowired
	public OrderService(AuthUtils authUtils, OrderRepository orderRepository, OrderItemRepository orderItemRepository, CartRepository cartRepository, ModelMapper modelMapper, PaymentRepository paymentRepository, CartItemRepository cartItemRepository) {
		this.authUtils = authUtils;
		this.orderRepository = orderRepository;
		this.cartRepository = cartRepository;
		this.modelMapper = modelMapper;
		this.paymentRepository = paymentRepository;
		this.cartItemRepository = cartItemRepository;
	}

	@Transactional
	public OrderDTO place(OrderRequest orderRequest) {
		User user = authUtils.loggedInUser();

		Cart cart = cartRepository.findByUserId(user.getUserId()).orElseThrow(
				() -> new ResourceNotFoundException("Cart", "userId", user.getUserId())
		);

		if(cart.getCartItems().isEmpty()) throw new ApiException("Cart is empty");

		Order order = new Order();

		order.setUser(user);
		order.setOrderDate(LocalDateTime.now());
		order.setTotalAmount(cart.getTotalPrice());
		order.setOrderStatus("ACCEPTED");

		Payment payment = new Payment(
				orderRequest.getPaymentMethod(),
				orderRequest.getPgName(),
				orderRequest.getPgPaymentId(),
				orderRequest.getPgStatus(),
				orderRequest.getPgResponseMessage()
		);

		payment.setOrder(order);
		paymentRepository.save(payment);

		order.setPayment(payment);

		List<CartItem> cartItems = cart.getCartItems();
		List<OrderItem> orderItems = new ArrayList<>();

		cartItems.forEach(ci -> {
			OrderItem orderItem = new OrderItem();
			orderItem.setProduct(ci.getProduct());
			orderItem.setQuantity(ci.getQuantity());
			orderItem.setDiscount(ci.getDiscount());
			orderItem.setTotalPrice(ci.getProductPrice()
					.multiply(BigDecimal.valueOf(ci.getQuantity()))
			);
			orderItem.setOrder(order);
			orderItems.add(orderItem);
		});

		order.setOrderItems(orderItems);
		orderRepository.save(order);

		cartItemRepository.empty(cart.getCartId());
		cartRepository.deleteForUser(user.getUserId());

		OrderDTO orderDTO = modelMapper.map(order, OrderDTO.class);
		orderDTO.setPayment(modelMapper.map(payment, PaymentDTO.class));

		orderItems.forEach(item -> {
			OrderItemDTO orderItemDTO = modelMapper.map(item, OrderItemDTO.class);
			orderItemDTO.setProductDTO(modelMapper.map(item.getProduct(), ProductDTO.class));
			orderItemDTO.setQuantity(item.getQuantity());
			orderDTO.getOrderItems().add(orderItemDTO);
		});

		return orderDTO;
	}

}

package com.winet.ecommerce.service;

import com.winet.ecommerce.exception.custom.ApiException;
import com.winet.ecommerce.exception.custom.ResourceNotFoundException;
import com.winet.ecommerce.model.*;
import com.winet.ecommerce.payload.dto.OrderDTO;
import com.winet.ecommerce.payload.dto.OrderRequest;
import com.winet.ecommerce.repository.CartItemRepository;
import com.winet.ecommerce.repository.CartRepository;
import com.winet.ecommerce.repository.OrderRepository;
import com.winet.ecommerce.repository.PaymentRepository;
import com.winet.ecommerce.util.AuthUtils;
import com.winet.ecommerce.util.DtoUtils;
import jakarta.transaction.Transactional;
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
	private final PaymentRepository paymentRepository;
	private final CartRepository cartRepository;
	private final CartItemRepository cartItemRepository;
	private final DtoUtils dtoUtils;

	@Autowired
	public OrderService(AuthUtils authUtils, OrderRepository orderRepository, PaymentRepository paymentRepository, CartRepository cartRepository, CartItemRepository cartItemRepository, DtoUtils dtoUtils) {
		this.authUtils = authUtils;
		this.orderRepository = orderRepository;
		this.paymentRepository = paymentRepository;
		this.cartRepository = cartRepository;
		this.cartItemRepository = cartItemRepository;
		this.dtoUtils = dtoUtils;
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

		List<OrderItem> orderItems = new ArrayList<>();

		cart.getCartItems().forEach(ci -> {
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

		return dtoUtils.convertOrderToDTO(order);
	}

}

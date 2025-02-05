package com.winet.ecommerce.payload.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDTO {

	private Long orderId;
	private Long userId;
	private List<OrderItemDTO> orderItems;
	private PaymentDTO payment;
	private LocalDateTime orderDate;
	private BigDecimal totalAmount;
	private String orderStatus;

}

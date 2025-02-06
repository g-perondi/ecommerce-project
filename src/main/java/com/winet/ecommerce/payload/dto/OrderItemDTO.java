package com.winet.ecommerce.payload.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemDTO {

	private Long orderItemId;
	private Integer quantity;
	private BigDecimal totalPrice;
	private ProductDTO product;

}

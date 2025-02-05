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
	private ProductDTO productDTO;
	private Integer quantity;
	private Double discount;
	private BigDecimal totalPrice;

}

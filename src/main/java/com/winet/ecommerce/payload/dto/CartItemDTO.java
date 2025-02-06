package com.winet.ecommerce.payload.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItemDTO {

	private Long cartItemId;
	private Integer quantity;
	private BigDecimal productPrice;
	private Double discount;
	private ProductDTO product;

}

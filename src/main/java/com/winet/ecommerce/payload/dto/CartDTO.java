package com.winet.ecommerce.payload.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartDTO {

	private Long cartId;
	private BigDecimal totalPrice = BigDecimal.ZERO;
	private List<CartItemDTO> cartItems;

}

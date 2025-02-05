package com.winet.ecommerce.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class OrderItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long orderItemId;

	@ManyToOne
	@JoinColumn(name = "product_id")
	private Product product;

	private Integer quantity;
	private Double discount;
	private BigDecimal totalPrice;

	@ManyToOne
	@JoinColumn(name = "order_id")
	private Order order;

}

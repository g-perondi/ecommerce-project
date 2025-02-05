package com.winet.ecommerce.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "orders")
public class Order {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long orderId;

	private BigDecimal totalAmount;

	@Temporal(TemporalType.TIMESTAMP)
	private LocalDateTime orderDate;

	private String orderStatus;

	@ToString.Exclude
	@OneToMany(
			mappedBy = "order",
			cascade = { CascadeType.PERSIST, CascadeType.MERGE }
	)
	List<OrderItem> orderItems = new ArrayList<>();

	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;

	@OneToOne
	@JoinColumn(name = "payment_id")
	private Payment payment;

}

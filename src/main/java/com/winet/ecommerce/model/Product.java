package com.winet.ecommerce.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Product {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long productId;

	@NotNull
	@NotBlank
	@Size(min = 2)
	private String productName;

	@Size(min = 6)
	private String description;

	@Positive
	private BigDecimal price;

	private BigDecimal specialPrice;

	@PositiveOrZero
	private Double discount;

	private String image;

	@ToString.Exclude
	@OneToMany(
			mappedBy = "product",
			cascade = { CascadeType.PERSIST, CascadeType.MERGE },
			fetch = FetchType.EAGER
	)
	List<CartItem> cartItems;

	public Product(Long productId, String productName, String description, BigDecimal price, BigDecimal specialPrice, Double discount, String image) {
		this.productId = productId;
		this.productName = productName;
		this.description = description;
		this.price = price;
		this.specialPrice = specialPrice;
		this.discount = discount;
		this.image = image;
	}

}

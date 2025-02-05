package com.winet.ecommerce.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
public class Payment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long paymentId;

	@NotNull
	@OneToOne(
			mappedBy = "payment",
			cascade = { CascadeType.PERSIST, CascadeType.MERGE }
	)
	private Order order;

	private String paymentMethod;

	// i.e. Stripe, Paypal, ...
	private String paymentGatewayName;

	private String pgPaymentId;
	private String pgStatus;
	private String pgResponseMessage;

	public Payment(String paymentMethod, String paymentGatewayName, String pgPaymentId, String pgStatus, String pgResponseMessage) {
		this.paymentMethod = paymentMethod;
		this.paymentGatewayName = paymentGatewayName;
		this.pgPaymentId = pgPaymentId;
		this.pgStatus = pgStatus;
		this.pgResponseMessage = pgResponseMessage;
	}

}

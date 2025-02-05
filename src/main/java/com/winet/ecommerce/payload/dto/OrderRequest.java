package com.winet.ecommerce.payload.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequest {

	private String paymentMethod;
	private String pgName;
	private String pgPaymentId;
	private String pgStatus;
	private String pgResponseMessage;

}

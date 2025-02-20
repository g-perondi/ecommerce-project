package com.winet.ecommerce.payload.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExchangeData {

	private String base;
	private String target;
	private double mid;
	private int unit;
	private String timestamp;

}

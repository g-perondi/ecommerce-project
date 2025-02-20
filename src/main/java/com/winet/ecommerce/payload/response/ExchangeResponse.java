package com.winet.ecommerce.payload.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExchangeResponse {

	@JsonProperty("status_code")
	private int statusCode;

	@JsonProperty("data")
	private ExchangeData data;

}

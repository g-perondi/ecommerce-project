package com.winet.ecommerce.service;

import com.winet.ecommerce.exception.custom.ApiException;
import com.winet.ecommerce.payload.response.ExchangeResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Service
public class CurrencyService {

	private final RestTemplate restTemplate;

	@Autowired
	public CurrencyService(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	public BigDecimal fetchExchangeRate(String from, String to) {
		String url = String.format("https://hexarate.paikama.co/api/rates/latest/%s?target=%s", from, to);
		ExchangeResponse response = restTemplate.getForObject(url, ExchangeResponse.class);

		if(response != null && response.getData() != null) {
			return BigDecimal.valueOf(response.getData().getMid());
		}
		throw new ApiException("Error while fetching exchange rate");
	}

	public BigDecimal convert(BigDecimal value, String from, String to) {
		BigDecimal rate = fetchExchangeRate(from, to);
		return rate.multiply(value);
	}

}

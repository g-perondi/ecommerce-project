package com.winet.ecommerce.exception.custom;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ApiException extends RuntimeException {

	@Serial
	private static final long serialVersionUID = 1L;

	public ApiException(String message) {
		super(message);
	}

}




package com.winet.ecommerce.exception.custom;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ResourceNotFoundException extends RuntimeException {

	@Serial
	private static final long serialVersionUID = 1L;

	private String resourceName;
	private String field;
	private String fieldName;
	private Long fieldId;

	public ResourceNotFoundException(String resourceName, String field, String fieldName) {
		super(String.format("%s not found with %s: %s", resourceName, field, fieldName));
		this.resourceName = resourceName;
		this.field = field;
		this.fieldName = fieldName;
	}

	public ResourceNotFoundException(String resourceName, String field, Long fieldId) {
		super(String.format("%s not found with %s: %d", resourceName, field, fieldId));
		this.resourceName = resourceName;
		this.field = field;
		this.fieldId = fieldId;
	}

}

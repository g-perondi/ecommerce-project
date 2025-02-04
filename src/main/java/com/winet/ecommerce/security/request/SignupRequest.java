package com.winet.ecommerce.security.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {

	@NotBlank
	@NotNull
	@Size(min = 2, max = 20)
	private String username;

	@NotBlank
	@NotNull
	@Email
	@Size(min = 6, max = 50)
	private String email;

	@NotBlank
	@NotNull
	@Size(min = 6, max = 30)
	private String password;

	private String role;

}

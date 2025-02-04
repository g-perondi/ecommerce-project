package com.winet.ecommerce.security.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoResponse {

	private Long userId;
	private String email;
	private String username;
	private List<String> authorities;

}

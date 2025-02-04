package com.winet.ecommerce.security.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.winet.ecommerce.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDetailsImpl implements UserDetails {

	@Serial
	private static final long serialVersionUID = 1L;

	private Long id;
	private String username;
	private String email;

	@JsonIgnore
	private String password;

	private List<SimpleGrantedAuthority> authorities;

	public static UserDetailsImpl build(User user) {
		List<SimpleGrantedAuthority> authorities = List.of(
				new SimpleGrantedAuthority("ROLE_USER")
		);

		return new UserDetailsImpl(
				user.getUserId(),
				user.getUsername(),
				user.getEmail(),
				user.getPassword(),
				authorities
		);
	}

}

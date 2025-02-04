package com.winet.ecommerce.util;

import com.winet.ecommerce.model.User;
import com.winet.ecommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class AuthUtils {

	private final UserRepository userRepository;

	@Autowired
	public AuthUtils(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public String loggedInEmail() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = userRepository.findByUsername(auth.getName())
				.orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + auth.getName()));
		return user.getEmail();
	}

	public User loggedInUser() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		return userRepository.findByUsername(auth.getName())
				.orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + auth.getName()));
	}

}

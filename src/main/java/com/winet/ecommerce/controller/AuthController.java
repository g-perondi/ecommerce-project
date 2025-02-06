package com.winet.ecommerce.controller;

import com.winet.ecommerce.model.RoleName;
import com.winet.ecommerce.model.User;
import com.winet.ecommerce.payload.response.ApiResponse;
import com.winet.ecommerce.repository.UserRepository;
import com.winet.ecommerce.security.jwt.JwtUtils;
import com.winet.ecommerce.security.request.LoginRequest;
import com.winet.ecommerce.security.request.SignupRequest;
import com.winet.ecommerce.security.response.UserInfoResponse;
import com.winet.ecommerce.security.service.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/auth")
public class AuthController {

	private final JwtUtils jwtUtils;
	private final AuthenticationManager authenticationManager;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Autowired
	public AuthController(JwtUtils jwtUtils, AuthenticationManager authenticationManager, UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.jwtUtils = jwtUtils;
		this.authenticationManager = authenticationManager;
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@PostMapping("/sign-in")
	public ResponseEntity<ApiResponse> authenticateUser(@RequestBody LoginRequest loginRequest) {

		Authentication authentication;
		try {
			authentication = authenticationManager
					.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
		} catch(AuthenticationException exception) {
			return new ResponseEntity<>(new ApiResponse("Bad credentials", false), HttpStatus.NOT_FOUND);
		}

		SecurityContextHolder.getContext().setAuthentication(authentication);

		UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

		ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);

		List<String> roles = userDetails.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.toList();

		UserInfoResponse response = new UserInfoResponse(userDetails.getId(), userDetails.getEmail(), userDetails.getUsername(), roles);

		return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString()).body(response);
	}

	@PostMapping("/sign-up")
	public ResponseEntity<ApiResponse> registerUser(@Valid @RequestBody SignupRequest signupRequest) {
		if(userRepository.existsByUsername(signupRequest.getUsername())) {
			return ResponseEntity.badRequest().body(new ApiResponse("Username already exists", false));
		}

		if(userRepository.existsByEmail(signupRequest.getEmail())) {
			return ResponseEntity.badRequest().body(new ApiResponse("Email already in use", false));
		}

		User user = new User(
				signupRequest.getUsername(),
				signupRequest.getEmail(),
				passwordEncoder.encode(signupRequest.getPassword())
		);

		String role = signupRequest.getRole();

		if(role == null) {
			user.setRole(RoleName.ROLE_USER.toString());
		} else if(role.equalsIgnoreCase(RoleName.ROLE_ADMIN.toString())) {
			user.setRole(RoleName.ROLE_ADMIN.toString());
		} else {
			user.setRole(RoleName.ROLE_USER.toString());
		}

		userRepository.save(user);
		return ResponseEntity.ok().body(new ApiResponse("User registered successfully", true));
	}

	@PostMapping("/sign-out")
	public ResponseEntity<ApiResponse> logout() {
		ResponseCookie cleanJwtCookie = jwtUtils.getCleanJwtCookie();
		return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cleanJwtCookie.toString())
				.body(new ApiResponse("Logged out successfully", true));
	}

	@GetMapping("/username")
	public ResponseEntity<?> getCurrentUsername(Authentication authentication) {
		if(authentication == null) {
			return new ResponseEntity<>("Authentication required", HttpStatus.NOT_FOUND);
		}
		return ResponseEntity.ok(authentication.getName());
	}

	@GetMapping("/user")
	public ResponseEntity<?> getCurrentUser(Authentication authentication) {

		if(authentication == null) return new ResponseEntity<>("Authentication required", HttpStatus.UNAUTHORIZED);

		UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

		List<String> roles = userDetails.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.toList();

		UserInfoResponse response = new UserInfoResponse(userDetails.getId(), userDetails.getEmail(), userDetails.getUsername(), roles);

		return ResponseEntity.ok(response);
	}


}

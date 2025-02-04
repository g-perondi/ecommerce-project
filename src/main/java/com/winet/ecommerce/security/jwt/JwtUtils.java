package com.winet.ecommerce.security.jwt;

import com.winet.ecommerce.security.service.UserDetailsImpl;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(JwtUtils.class);

	@Value("${spring.app.jwt.secret}")
	private String jwtSecret;

	@Value("${spring.app.jwt.expiration}")
	private Long jwtExpirationMs;

	@Value("${spring.app.jwt.name}")
	private String jwtCookieName;

	public boolean validateJwtToken(String token) {
		try {
			Jwts.parser()
					.verifyWith((SecretKey) key())
					.build()
					.parseSignedClaims(token);
			return true;
		} catch(MalformedJwtException e) {
			LOGGER.error("Invalid JWT token: {}", e.getMessage());
		} catch(ExpiredJwtException e) {
			LOGGER.error("Expired JWT token: {}", e.getMessage());
		} catch(UnsupportedJwtException e) {
			LOGGER.error("Unsupported JWT token: {}", e.getMessage());
		} catch(IllegalArgumentException e) {
			LOGGER.error("JWT claims string is empty: {}", e.getMessage());
		}
		return false;
	}

	public ResponseCookie generateJwtCookie(UserDetailsImpl userDetails) {
		String jwt = generateTokenFromUsername(userDetails.getUsername());
		return ResponseCookie.from(jwtCookieName, jwt)
				.path("/api")
				.maxAge(jwtExpirationMs / 1000)
				.httpOnly(false)
				.build();
	}

	public String getJwtFromCookie(HttpServletRequest request) {
		Cookie cookie = WebUtils.getCookie(request, jwtCookieName);
		if(cookie != null) {
			return cookie.getValue();
		} else {
			return null;
		}
	}

	public ResponseCookie getCleanJwtCookie() {
		return ResponseCookie.from(jwtCookieName)
				.path("/api")
				.build();
	}

	public String getUsernameFromJwtToken(String token) {
		return Jwts.parser()
				.verifyWith((SecretKey) key())
				.build()
				.parseSignedClaims(token)
				.getPayload().getSubject();
	}

	private String generateTokenFromUsername(String username) {
		return Jwts.builder()
				.subject(username)
				.issuedAt(new Date())
				.expiration(new Date(new Date().getTime() + jwtExpirationMs))
				.signWith(key())
				.compact();
	}

	private Key key() {
		return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
	}

}

package com.banking_system.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import com.banking_system.config.JwtProperties;
import com.banking_system.model.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

	private final SecretKey secretKey;
	private final int expirationMinutes;

	public JwtService(JwtProperties jwtProperties) {
		this.secretKey = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
		this.expirationMinutes = jwtProperties.expirationMinutes();
	}

	public String generateToken(User user) {
		Instant now = Instant.now();
		Instant expiration = now.plusSeconds(expirationMinutes * 60L);

		return Jwts.builder()
				.subject(user.getEmail())
				.claim("role", user.getRole().name())
				.claim("userId", user.getId().toHexString())
				.issuedAt(Date.from(now))
				.expiration(Date.from(expiration))
				.signWith(secretKey)
				.compact();
	}

	public Claims validateAndParseClaims(String token) {
		return Jwts.parser()
				.verifyWith(secretKey)
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}

	public String extractEmail(String token) {
		return validateAndParseClaims(token).getSubject();
	}
}

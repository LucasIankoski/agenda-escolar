package com.escolar.agenda.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

	@Value("${jwt.secret}")
	private String secret;

	@Value("${jwt.expiration}")
	private long expirationMs;

	public long getExpirationMs() {
		return expirationMs;
	}

	public String generateToken(UserDetails userDetails) {
		return generateToken(Map.of(), userDetails);
	}

	public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
		Date now = new Date();
		Date exp = new Date(now.getTime() + expirationMs);

		return Jwts.builder()
				.setClaims(extraClaims)
				.setSubject(userDetails.getUsername()) // email
				.setIssuedAt(now)
				.setExpiration(exp)
				.signWith(getSigningKey(), SignatureAlgorithm.HS256)
				.compact();
	}

	public String extractUsername(String token) {
		return extractAllClaims(token).getSubject();
	}

	public boolean isTokenValid(String token, UserDetails userDetails) {
		final String username = extractUsername(token);
		return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
	}

	private boolean isTokenExpired(String token) {
		return extractAllClaims(token).getExpiration().before(new Date());
	}

	private Claims extractAllClaims(String token) {
		return Jwts.parserBuilder()
				.setSigningKey(getSigningKey())
				.build()
				.parseClaimsJws(token)
				.getBody();
	}

	private Key getSigningKey() {
		byte[] keyBytes = Decoders.BASE64.decode(toBase64IfNeeded(secret));
		return Keys.hmacShaKeyFor(keyBytes);
	}

	// Permite você usar secret "normal" no yml; se não for Base64, converte.
	private String toBase64IfNeeded(String raw) {
		// Heurística simples: se decodificação falhar, converte para Base64.
		try {
			Decoders.BASE64.decode(raw);
			return raw;
		} catch (Exception e) {
			return java.util.Base64.getEncoder().encodeToString(raw.getBytes());
		}
	}
}


package com.escolar.agenda.security;

import com.escolar.agenda.entity.UserApp;
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
import java.util.UUID;

@Service
public class JwtService {

	@Value("${jwt.secret}")
	private String secret;

	@Value("${jwt.expiration}")
	private long expirationMs;

	public long getExpirationMs() {
		return expirationMs;
	}

	public String generateToken(UserApp user) {
		java.util.HashMap<String, Object> claims = new java.util.HashMap<>();
		claims.put("userId", user.getId().toString());
		claims.put("email", user.getEmail());
		claims.put("type", user.getType().name());

		if (user.getSchool() != null) {
			claims.put("schoolId", user.getSchool().getId().toString());
			claims.put("schoolSlug", user.getSchool().getSlug());
		}

		return generateToken(claims, user);
	}

	public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
		Date now = new Date();
		Date exp = new Date(now.getTime() + expirationMs);

		return Jwts.builder()
				.setClaims(extraClaims)
				.setSubject(resolveSubject(userDetails))
				.setIssuedAt(now)
				.setExpiration(exp)
				.signWith(getSigningKey(), SignatureAlgorithm.HS256)
				.compact();
	}

	public UUID extractUserId(String token) {
		return UUID.fromString(extractAllClaims(token).getSubject());
	}

	public UUID extractSchoolId(String token) {
		String schoolId = extractAllClaims(token).get("schoolId", String.class);
		return schoolId == null ? null : UUID.fromString(schoolId);
	}

	public boolean isTokenValid(String token, UserApp user) {
		UUID tokenSchoolId = extractSchoolId(token);
		UUID userSchoolId = user.getSchool() != null ? user.getSchool().getId() : null;
		return extractUserId(token).equals(user.getId())
				&& java.util.Objects.equals(tokenSchoolId, userSchoolId)
				&& !isTokenExpired(token);
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

	private String toBase64IfNeeded(String raw) {
		try {
			Decoders.BASE64.decode(raw);
			return raw;
		} catch (Exception e) {
			return java.util.Base64.getEncoder().encodeToString(raw.getBytes());
		}
	}

	private String resolveSubject(UserDetails userDetails) {
		if (userDetails instanceof UserApp user) {
			return user.getId().toString();
		}
		return userDetails.getUsername();
	}
}

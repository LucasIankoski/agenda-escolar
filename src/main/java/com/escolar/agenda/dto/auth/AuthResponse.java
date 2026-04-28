package com.escolar.agenda.dto.auth;

import com.escolar.agenda.enums.UserType;

import java.util.UUID;

public record AuthResponse(
		String token,
		Long expiresInMs,
		UUID schoolId,
		String schoolSlug,
		UUID userId,
		String email,
		UserType type
) {
	public AuthResponse(String token, Long expiresInMs) {
		this(token, expiresInMs, null, null, null, null, null);
	}
}

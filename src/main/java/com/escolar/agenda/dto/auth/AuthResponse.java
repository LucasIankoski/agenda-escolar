package com.escolar.agenda.dto.auth;

public record AuthResponse(
		String token,
		Long expiresInMs
) {}

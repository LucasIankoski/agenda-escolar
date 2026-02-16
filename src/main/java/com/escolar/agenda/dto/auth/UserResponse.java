package com.escolar.agenda.dto.auth;

import com.escolar.agenda.enums.UserType;

import java.util.UUID;

public record UserResponse(
		UUID id,
		String name,
		String email,
		UserType type,
		boolean active
) {}

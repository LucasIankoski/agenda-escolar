package com.escolar.agenda.dto.auth;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
		@NotBlank @JsonAlias("email") String login,
		@NotBlank String password,
		String schoolSlug
) {}

package com.escolar.agenda.dto.auth;

import com.escolar.agenda.enums.UserType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserCreateRequest(
		@NotBlank String name,
		@NotBlank String email,
		@NotBlank @Size(min = 5) String password,
		@NotNull UserType type
) {
}

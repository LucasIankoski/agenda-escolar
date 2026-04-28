package com.escolar.agenda.dto.platform;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SchoolAdminCreateRequest(
		@NotBlank String name,
		@NotBlank String email,
		@NotBlank @Size(min = 5) String password
) {
}

package com.escolar.agenda.dto.platform;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PlatformSchoolCreateRequest(
		@NotBlank String schoolName,
		@NotBlank String schoolSlug,
		@NotNull @Valid SchoolAdminCreateRequest admin
) {
}

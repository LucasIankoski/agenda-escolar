package com.escolar.agenda.dto.classroom;

import jakarta.validation.constraints.NotBlank;

public record ClassroomUpdateRequest(
		@NotBlank String name,
		boolean active
) {}

package com.escolar.agenda.dto.classroom;

import jakarta.validation.constraints.NotBlank;

public record ClassroomCreateRequest(
		@NotBlank String name
) {}

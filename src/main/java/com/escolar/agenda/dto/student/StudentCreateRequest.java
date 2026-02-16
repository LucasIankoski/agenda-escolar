package com.escolar.agenda.dto.student;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.sql.Timestamp;
import java.util.UUID;

public record StudentCreateRequest(
		@NotBlank String name,
		@NotBlank String lastName,
		Timestamp birthDate,
		@NotNull UUID classroomId
) {}
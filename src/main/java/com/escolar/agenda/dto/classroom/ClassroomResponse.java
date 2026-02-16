package com.escolar.agenda.dto.classroom;

import java.util.UUID;

public record ClassroomResponse(
		UUID id,
		String name,
		boolean active
) {}

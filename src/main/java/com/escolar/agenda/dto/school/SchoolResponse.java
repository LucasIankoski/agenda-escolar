package com.escolar.agenda.dto.school;

import java.util.UUID;

public record SchoolResponse(
		UUID id,
		String name,
		String slug,
		boolean active
) {
}

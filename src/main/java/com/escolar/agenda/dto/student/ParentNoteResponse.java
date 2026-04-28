package com.escolar.agenda.dto.student;

import java.time.LocalDateTime;
import java.util.UUID;

public record ParentNoteResponse(
		UUID id,
		UUID studentId,
		UUID createdById,
		String createdByName,
		String message,
		LocalDateTime createdAt,
		boolean read,
		LocalDateTime readAt,
		UUID readById
) {}

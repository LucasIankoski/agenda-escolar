package com.escolar.agenda.dto.student;

import java.sql.Timestamp;
import java.util.UUID;

public record StudentResponse(
		UUID id,
		String name,
		String lastName,
		Timestamp birthDate,
		UUID classroomId,
		UUID parentUserId,
		String parentName,
		String parentLastName,
		String parentContact,
		String parentEmail,
		long pendingParentNoteCount
) {}

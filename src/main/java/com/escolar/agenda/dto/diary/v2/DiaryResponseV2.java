package com.escolar.agenda.dto.diary.v2;

import java.time.LocalDateTime;
import java.util.UUID;

public record DiaryResponseV2(
		UUID id,
		UUID studentId,
		UUID createdById,
		String createdByName,
		LocalDateTime createdAt,
		DiaryV2Payload payload,
		boolean read,
		LocalDateTime readAt,
		UUID readById
) {
}

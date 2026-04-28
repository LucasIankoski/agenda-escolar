package com.escolar.agenda.dto.student;

import java.time.LocalDateTime;
import java.util.UUID;

public record StudentGalleryPhotoResponse(
		UUID id,
		UUID studentId,
		UUID createdById,
		String createdByName,
		String caption,
		LocalDateTime createdAt,
		Long sizeInBytes,
		Integer width,
		Integer height,
		String imageUrl,
		String thumbnailUrl
) {
}

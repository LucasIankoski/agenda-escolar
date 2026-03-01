package com.escolar.agenda.dto.diary.v2;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record DiaryCreateV2Request(
		@NotNull UUID studentId,
		@NotNull @Valid DiaryV2Payload payload
) {
}

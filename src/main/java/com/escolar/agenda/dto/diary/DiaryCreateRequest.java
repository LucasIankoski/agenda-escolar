package com.escolar.agenda.dto.diary;

import com.escolar.agenda.enums.FoodLevel;
import com.escolar.agenda.enums.HygieneType;
import com.escolar.agenda.enums.Mood;
import com.escolar.agenda.enums.StoolAspect;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record DiaryCreateRequest(
		@NotNull UUID studentId,

		@NotNull FoodLevel foodLevel,
		String foodNotes,

		LocalDateTime sleepStart,
		LocalDateTime sleepEnd,

		HygieneType hygieneType,
		Integer hygieneCount,
		Boolean pee,
		Boolean poop,
		StoolAspect stoolAspect,
		String hygieneNotes,

		@NotNull Mood mood,

		String activities
) {}

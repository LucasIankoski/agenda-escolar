package com.escolar.agenda.dto.diary;

import com.escolar.agenda.enums.FoodLevel;
import com.escolar.agenda.enums.HygieneType;
import com.escolar.agenda.enums.Mood;
import com.escolar.agenda.enums.StoolAspect;

import java.time.LocalDateTime;
import java.util.UUID;

public record DiaryResponse(
		UUID id,
		UUID studentId,

		UUID createdById,
		String createdByName,
		LocalDateTime createdAt,

		FoodLevel foodLevel,
		String foodNotes,

		LocalDateTime sleepStart,
		LocalDateTime sleepEnd,

		HygieneType hygieneType,
		Integer hygieneCount,
		Boolean pee,
		Boolean poop,
		StoolAspect stoolAspect,
		String hygieneNotes,

		Mood mood,
		String activities,

		boolean read,
		LocalDateTime readAt,
		UUID readById
) {}

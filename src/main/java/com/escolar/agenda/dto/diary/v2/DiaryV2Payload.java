package com.escolar.agenda.dto.diary.v2;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;
import java.util.Locale;

public record DiaryV2Payload(
		@NotNull @Valid Meals meals,
		@NotNull @Valid Sleep sleep,
		@NotNull @Valid PedagogicalProposals pedagogicalProposals,
		@NotNull @Valid Needs needs,
		@NotNull @Valid BringTomorrow bringTomorrow,
		@Size(max = 1000) String teacherNote
) {

	public enum MealStatus {
		BEM,
		METADE,
		MENOS_DA_METADE,
		RECUSOU;

		@JsonCreator
		public static MealStatus fromValue(String value) {
			if (value == null) {
				return null;
			}

			String normalized = value.trim()
					.toUpperCase(Locale.ROOT)
					.replaceAll("[^A-Z0-9]", "");

			return switch (normalized) {
				case "BEM" -> BEM;
				case "METADE" -> METADE;
				case "MENOSDAMETADE" -> MENOS_DA_METADE;
				case "RECUSOU" -> RECUSOU;
				default -> throw new IllegalArgumentException("MealStatus invalido: " + value);
			};
		}
	}

	public record Meals(
			MealStatus breakfast,
			MealStatus lunch,
			MealStatus bottle,
			MealStatus fruit,
			MealStatus dinner,
			MealStatus supper
	) {
	}

	public record Sleep(
			@NotNull @Valid SleepPeriod morning,
			@NotNull @Valid SleepPeriod afternoon
	) {
	}

	public record SleepPeriod(
			boolean slept,
			LocalTime startTime,
			LocalTime endTime
	) {
	}

	public record PedagogicalProposals(
			boolean pedagogicalActivity,
			boolean music,
			boolean patio,
			boolean freePlay
	) {
	}

	public record Needs(
			@NotNull @Valid CountableItem pee,
			@NotNull @Valid CountableItem poop
	) {
	}

	public record CountableItem(
			boolean selected,
			@Min(0) Integer count
	) {
	}

	public record BringTomorrow(
			boolean diaper,
			boolean wipes,
			boolean ointment,
			boolean toothpaste,
			@Size(max = 255) String other
	) {
	}
}

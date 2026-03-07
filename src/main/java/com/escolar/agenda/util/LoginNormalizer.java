package com.escolar.agenda.util;

import java.util.Locale;
import java.util.regex.Pattern;

public final class LoginNormalizer {

	private static final Pattern NON_DIGIT = Pattern.compile("\\D");
	private static final Pattern BR_MOBILE = Pattern.compile("^[1-9]{2}9\\d{8}$");
	private static final Pattern BR_MOBILE_WITH_COUNTRY = Pattern.compile("^55[1-9]{2}9\\d{8}$");

	private LoginNormalizer() {
	}

	public static String normalize(String rawLogin) {
		if (rawLogin == null) {
			throw new IllegalArgumentException("Login nao informado");
		}

		String trimmed = rawLogin.trim();
		if (trimmed.isEmpty()) {
			throw new IllegalArgumentException("Login nao informado");
		}

		if (trimmed.contains("@")) {
			return trimmed.toLowerCase(Locale.ROOT);
		}

		String digits = NON_DIGIT.matcher(trimmed).replaceAll("");
		if (digits.isEmpty()) {
			throw new IllegalArgumentException("Login nao informado");
		}
		return digits;
	}

	public static boolean isBrazilianMobilePhone(String normalizedPhone) {
		if (normalizedPhone == null || normalizedPhone.isBlank()) {
			return false;
		}
		return BR_MOBILE.matcher(normalizedPhone).matches()
				|| BR_MOBILE_WITH_COUNTRY.matcher(normalizedPhone).matches();
	}
}

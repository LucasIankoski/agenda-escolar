package com.escolar.agenda.security;

public record TenantUsername(String schoolSlug, String login) {

	public static final String PLATFORM_SLUG = "platform";
	private static final String SEPARATOR = "|";

	public static String encode(String schoolSlug, String login) {
		return schoolSlug + SEPARATOR + login;
	}

	public static String encodePlatform(String login) {
		return encode(PLATFORM_SLUG, login);
	}

	public static TenantUsername parse(String username) {
		if (username == null || !username.contains(SEPARATOR)) {
			throw new IllegalArgumentException("Login multi-tenant invalido");
		}

		String[] parts = username.split("\\|", 2);
		if (parts[0].isBlank() || parts[1].isBlank()) {
			throw new IllegalArgumentException("Login multi-tenant invalido");
		}

		return new TenantUsername(parts[0], parts[1]);
	}
}

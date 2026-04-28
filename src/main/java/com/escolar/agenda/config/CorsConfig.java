package com.escolar.agenda.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();

		config.setAllowedOrigins(List.of(
				"http://localhost:3000",
				"http://localhost:5173",
				"http://localhost:57225",
				"https://6122-2804-d51-7e05-9400-c430-9aa0-7d49-e0be.ngrok-free.app/"
				// "https://seu-frontend.com" (produção)
		));

		config.setAllowCredentials(true);

		config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

		config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "Origin"));

		config.setExposedHeaders(List.of("Authorization"));

		config.setMaxAge(3600L);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

		source.registerCorsConfiguration("/**", config);
		return source;
	}
}

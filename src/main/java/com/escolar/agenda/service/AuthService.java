package com.escolar.agenda.service;

import com.escolar.agenda.dto.auth.AuthResponse;
import com.escolar.agenda.dto.auth.LoginRequest;
import com.escolar.agenda.entity.School;
import com.escolar.agenda.entity.UserApp;
import com.escolar.agenda.repository.UserRepository;
import com.escolar.agenda.security.JwtService;
import com.escolar.agenda.security.TenantUsername;
import com.escolar.agenda.util.LoginNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository userRepository;
	private final SchoolService schoolService;
	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;

	public AuthResponse login(LoginRequest request) {
		String normalizedLogin = LoginNormalizer.normalize(request.login());
		String username = resolveAuthenticationUsername(request.schoolSlug(), normalizedLogin);
		Authentication auth = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(username, request.password())
		);

		UserApp user = (UserApp) auth.getPrincipal();
		String token = jwtService.generateToken(user);

		return toAuthResponse(user, token);
	}

	private String resolveAuthenticationUsername(String schoolSlug, String normalizedLogin) {
		if (isPlatformLogin(schoolSlug)) {
			return TenantUsername.encodePlatform(normalizedLogin);
		}

		if (isBlank(schoolSlug) && userRepository.findBySchoolIsNullAndEmail(normalizedLogin).isPresent()) {
			return TenantUsername.encodePlatform(normalizedLogin);
		}

		School school = schoolService.resolveForLogin(schoolSlug);
		return TenantUsername.encode(school.getSlug(), normalizedLogin);
	}

	private AuthResponse toAuthResponse(UserApp user, String token) {
		School school = user.getSchool();
		return new AuthResponse(
				token,
				jwtService.getExpirationMs(),
				school != null ? school.getId() : null,
				school != null ? school.getSlug() : null,
				user.getId(),
				user.getEmail(),
				user.getType()
		);
	}

	private boolean isPlatformLogin(String schoolSlug) {
		return schoolSlug != null && TenantUsername.PLATFORM_SLUG.equalsIgnoreCase(schoolSlug.trim());
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}
}

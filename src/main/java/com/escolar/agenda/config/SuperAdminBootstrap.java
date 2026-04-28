package com.escolar.agenda.config;

import com.escolar.agenda.entity.UserApp;
import com.escolar.agenda.enums.UserType;
import com.escolar.agenda.repository.UserRepository;
import com.escolar.agenda.service.UserService;
import com.escolar.agenda.util.LoginNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SuperAdminBootstrap implements ApplicationRunner {

	private final SuperAdminBootstrapProperties properties;
	private final UserRepository userRepository;
	private final UserService userService;

	@Override
	public void run(ApplicationArguments args) {
		if (isBlank(properties.getEmail()) || isBlank(properties.getPassword())) {
			return;
		}

		String normalizedEmail = LoginNormalizer.normalize(properties.getEmail());
		if (userRepository.existsBySchoolIsNullAndEmail(normalizedEmail)) {
			return;
		}

		UserApp superAdmin = UserApp.builder()
				.school(null)
				.name(resolveName())
				.email(normalizedEmail)
				.password(properties.getPassword())
				.type(UserType.SUPER_ADMIN)
				.active(true)
				.build();

		userService.create(superAdmin);
	}

	private String resolveName() {
		if (!isBlank(properties.getName())) {
			return properties.getName().trim();
		}
		return "Super Admin";
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}
}

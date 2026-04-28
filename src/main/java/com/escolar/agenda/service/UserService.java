package com.escolar.agenda.service;

import com.escolar.agenda.dto.auth.UserCreateRequest;
import com.escolar.agenda.dto.auth.UserResponse;
import com.escolar.agenda.entity.School;
import com.escolar.agenda.entity.UserApp;
import com.escolar.agenda.enums.UserType;
import com.escolar.agenda.repository.UserRepository;
import com.escolar.agenda.util.LoginNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public UserApp getByIdOrThrow(UUID id, UserApp loggedUser) {
		return userRepository.findByIdAndSchoolId(id, loggedUser.getSchool().getId())
				.orElseThrow(() -> new NoSuchElementException("Usuario nao encontrado"));
	}

	public List<UserResponse> list(UserApp loggedUser) {
		return userRepository.findAllBySchoolId(loggedUser.getSchool().getId()).stream()
				.map(this::toResponse)
				.toList();
	}

	public UserResponse createSchoolUser(UserCreateRequest request, UserApp loggedUser) {
		validateSchoolUserType(request.type());

		School school = loggedUser.getSchool();
		String normalizedEmail = LoginNormalizer.normalize(request.email());
		if (userRepository.existsBySchoolIdAndEmail(school.getId(), normalizedEmail)) {
			throw new IllegalArgumentException("Email ja cadastrado nesta escola");
		}

		UserApp user = UserApp.builder()
				.school(school)
				.name(request.name().trim())
				.email(normalizedEmail)
				.password(request.password())
				.type(request.type())
				.active(true)
				.build();

		return toResponse(create(user));
	}

	public UserResponse find(UUID id, UserApp loggedUser) {
		return toResponse(getByIdOrThrow(id, loggedUser));
	}

	public UserResponse disable(UUID id, UserApp loggedUser) {
		UserApp u = getByIdOrThrow(id, loggedUser);
		u.setActive(false);
		return toResponse(userRepository.save(u));
	}

	public UserApp create(UserApp userWithPlainPassword) {
		userWithPlainPassword.setPassword(passwordEncoder.encode(userWithPlainPassword.getPassword()));
		return userRepository.save(userWithPlainPassword);
	}

	public UserResponse toResponse(UserApp u) {
		School school = u.getSchool();
		return new UserResponse(
				u.getId(),
				u.getName(),
				u.getEmail(),
				u.getType(),
				u.isEnabled(),
				school != null ? school.getId() : null,
				school != null ? school.getName() : null,
				school != null ? school.getSlug() : null
		);
	}

	private void validateSchoolUserType(UserType type) {
		if (type != UserType.ADMIN && type != UserType.PROFESSOR) {
			throw new IllegalArgumentException("A escola pode cadastrar apenas usuarios ADMIN ou PROFESSOR");
		}
	}
}

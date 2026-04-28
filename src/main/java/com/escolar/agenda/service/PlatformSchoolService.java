package com.escolar.agenda.service;

import com.escolar.agenda.dto.auth.UserResponse;
import com.escolar.agenda.dto.platform.PlatformSchoolCreateRequest;
import com.escolar.agenda.dto.platform.PlatformSchoolCreatedResponse;
import com.escolar.agenda.dto.platform.SchoolAdminCreateRequest;
import com.escolar.agenda.dto.school.SchoolResponse;
import com.escolar.agenda.entity.School;
import com.escolar.agenda.entity.UserApp;
import com.escolar.agenda.enums.UserType;
import com.escolar.agenda.repository.UserRepository;
import com.escolar.agenda.util.LoginNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlatformSchoolService {

	private final SchoolService schoolService;
	private final UserService userService;
	private final UserRepository userRepository;

	@Transactional(readOnly = true)
	public List<SchoolResponse> listSchools() {
		return schoolService.list();
	}

	@Transactional
	public PlatformSchoolCreatedResponse createSchoolWithAdmin(PlatformSchoolCreateRequest request) {
		School school = schoolService.create(request.schoolName(), request.schoolSlug());
		UserResponse admin = createAdmin(school, request.admin());
		return new PlatformSchoolCreatedResponse(schoolService.toResponse(school), admin);
	}

	@Transactional
	public UserResponse createAdmin(UUID schoolId, SchoolAdminCreateRequest request) {
		School school = schoolService.getByIdOrThrow(schoolId);
		return createAdmin(school, request);
	}

	private UserResponse createAdmin(School school, SchoolAdminCreateRequest request) {
		String normalizedEmail = LoginNormalizer.normalize(request.email());
		if (userRepository.existsBySchoolIdAndEmail(school.getId(), normalizedEmail)) {
			throw new IllegalArgumentException("Email ja cadastrado nesta escola");
		}

		UserApp admin = UserApp.builder()
				.school(school)
				.name(request.name().trim())
				.email(normalizedEmail)
				.password(request.password())
				.type(UserType.ADMIN)
				.active(true)
				.build();

		return userService.toResponse(userService.create(admin));
	}
}

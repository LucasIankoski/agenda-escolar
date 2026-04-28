package com.escolar.agenda.controller;

import com.escolar.agenda.dto.auth.UserResponse;
import com.escolar.agenda.dto.platform.PlatformSchoolCreateRequest;
import com.escolar.agenda.dto.platform.PlatformSchoolCreatedResponse;
import com.escolar.agenda.dto.platform.SchoolAdminCreateRequest;
import com.escolar.agenda.dto.school.SchoolResponse;
import com.escolar.agenda.service.PlatformSchoolService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/platform/schools")
public class PlatformSchoolController {

	private final PlatformSchoolService platformSchoolService;

	@GetMapping
	@PreAuthorize("hasRole('SUPER_ADMIN')")
	public ResponseEntity<List<SchoolResponse>> list() {
		return ResponseEntity.ok(platformSchoolService.listSchools());
	}

	@PostMapping
	@PreAuthorize("hasRole('SUPER_ADMIN')")
	public ResponseEntity<PlatformSchoolCreatedResponse> create(@RequestBody @Valid PlatformSchoolCreateRequest request) {
		return ResponseEntity.ok(platformSchoolService.createSchoolWithAdmin(request));
	}

	@PostMapping("/{schoolId}/admins")
	@PreAuthorize("hasRole('SUPER_ADMIN')")
	public ResponseEntity<UserResponse> createAdmin(@PathVariable UUID schoolId,
													@RequestBody @Valid SchoolAdminCreateRequest request) {
		return ResponseEntity.ok(platformSchoolService.createAdmin(schoolId, request));
	}
}

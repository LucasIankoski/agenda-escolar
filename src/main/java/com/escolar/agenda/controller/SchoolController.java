package com.escolar.agenda.controller;

import com.escolar.agenda.dto.school.SchoolResponse;
import com.escolar.agenda.entity.UserApp;
import com.escolar.agenda.service.SchoolService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/schools")
public class SchoolController {

	private final SchoolService schoolService;

	@GetMapping("/me")
	public ResponseEntity<SchoolResponse> me(Authentication authentication) {
		UserApp logged = (UserApp) authentication.getPrincipal();
		return ResponseEntity.ok(schoolService.toResponse(logged.getSchool()));
	}
}

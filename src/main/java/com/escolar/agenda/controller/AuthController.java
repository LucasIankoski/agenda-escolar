package com.escolar.agenda.controller;

import com.escolar.agenda.dto.auth.AuthResponse;
import com.escolar.agenda.dto.auth.LoginRequest;
import com.escolar.agenda.dto.school.SchoolLoginOptionResponse;
import com.escolar.agenda.service.AuthService;
import com.escolar.agenda.service.SchoolService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

	private final AuthService authService;
	private final SchoolService schoolService;

	@GetMapping("/schools")
	public ResponseEntity<java.util.List<SchoolLoginOptionResponse>> schools() {
		return ResponseEntity.ok(schoolService.listLoginOptions());
	}

	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
		return ResponseEntity.ok(authService.login(request));
	}
}

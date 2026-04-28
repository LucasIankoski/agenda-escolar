package com.escolar.agenda.controller;

import com.escolar.agenda.dto.auth.UserCreateRequest;
import com.escolar.agenda.dto.auth.UserResponse;
import com.escolar.agenda.entity.UserApp;
import com.escolar.agenda.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

	private final UserService userService;

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<UserResponse> create(@RequestBody @Valid UserCreateRequest request,
											   Authentication authentication) {
		UserApp logged = (UserApp) authentication.getPrincipal();
		return ResponseEntity.ok(userService.createSchoolUser(request, logged));
	}

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<List<UserResponse>> list(Authentication authentication) {
		UserApp logged = (UserApp) authentication.getPrincipal();
		return ResponseEntity.ok(userService.list(logged));
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<UserResponse> find(@PathVariable UUID id, Authentication authentication) {
		UserApp logged = (UserApp) authentication.getPrincipal();
		return ResponseEntity.ok(userService.find(id, logged));
	}

	@PatchMapping("/{id}/desativar")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<UserResponse> disabled(@PathVariable UUID id, Authentication authentication) {
		UserApp logged = (UserApp) authentication.getPrincipal();
		return ResponseEntity.ok(userService.disable(id, logged));
	}
}

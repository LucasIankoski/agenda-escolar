package com.escolar.agenda.controller;

import com.escolar.agenda.dto.auth.UserResponse;
import com.escolar.agenda.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

	private final UserService userService;

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<List<UserResponse>> list() {
		return ResponseEntity.ok(userService.list());
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<UserResponse> find(@PathVariable UUID id) {
		return ResponseEntity.ok(userService.find(id));
	}

	@PatchMapping("/{id}/desativar")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<UserResponse> disabled(@PathVariable UUID id) {
		return ResponseEntity.ok(userService.disable(id));
	}
}

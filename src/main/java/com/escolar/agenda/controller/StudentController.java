package com.escolar.agenda.controller;

import com.escolar.agenda.dto.student.StudentCreateRequest;
import com.escolar.agenda.dto.student.StudentResponse;
import com.escolar.agenda.entity.UserApp;
import com.escolar.agenda.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/students")
public class StudentController {

	private final StudentService studentService;

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<StudentResponse> create(@RequestBody @Valid StudentCreateRequest req) {
		return ResponseEntity.ok(studentService.create(req));
	}

	@GetMapping
	@PreAuthorize("hasAnyRole('ADMIN','PROFESSOR','PAI')")
	public ResponseEntity<List<StudentResponse>> list(Authentication authentication) {
		UserApp logged = (UserApp) authentication.getPrincipal();
		return ResponseEntity.ok(studentService.list(logged));
	}

	@GetMapping("/by-responsible")
	@PreAuthorize("hasAnyRole('ADMIN','PROFESSOR','PAI')")
	public ResponseEntity<StudentResponse> getByResponsible(
			@RequestParam(value = "login", required = false) String login,
			Authentication authentication) {
		UserApp logged = (UserApp) authentication.getPrincipal();
		return ResponseEntity.ok(studentService.getByResponsible(login, logged));
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN','PROFESSOR','PAI')")
	public ResponseEntity<StudentResponse> get(@PathVariable UUID id, Authentication authentication) {
		UserApp logged = (UserApp) authentication.getPrincipal();
		return ResponseEntity.ok(studentService.get(id, logged));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Void> delete(@PathVariable UUID id) {
		studentService.delete(id);
		return ResponseEntity.noContent().build();
	}
}

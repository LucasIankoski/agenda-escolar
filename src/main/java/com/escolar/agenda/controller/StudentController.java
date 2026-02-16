package com.escolar.agenda.controller;

import com.escolar.agenda.dto.student.StudentCreateRequest;
import com.escolar.agenda.dto.student.StudentResponse;
import com.escolar.agenda.dto.student.StudentUpdateRequest;
import com.escolar.agenda.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/students")
public class StudentController {

	private final StudentService studentService;

	@PostMapping
	public ResponseEntity<StudentResponse> create(@RequestBody @Valid StudentCreateRequest req) {
		return ResponseEntity.ok(studentService.create(req));
	}

	@GetMapping
	public ResponseEntity<List<StudentResponse>> list() {
		return ResponseEntity.ok(studentService.list());
	}

	@GetMapping("/{id}")
	public ResponseEntity<StudentResponse> get(@PathVariable UUID id) {
		return ResponseEntity.ok(studentService.get(id));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable UUID id) {
		studentService.delete(id);
		return ResponseEntity.noContent().build();
	}
}

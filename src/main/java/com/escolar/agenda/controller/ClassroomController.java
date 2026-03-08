package com.escolar.agenda.controller;

import com.escolar.agenda.dto.classroom.ClassroomCreateRequest;
import com.escolar.agenda.dto.classroom.ClassroomResponse;
import com.escolar.agenda.dto.classroom.ClassroomUpdateRequest;
import com.escolar.agenda.dto.student.StudentResponse;
import com.escolar.agenda.service.ClassroomService;
import com.escolar.agenda.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/classrooms")
public class ClassroomController {

	private final ClassroomService classroomService;
	private final StudentService studentService;

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ClassroomResponse> create(@RequestBody @Valid ClassroomCreateRequest request) {
		return ResponseEntity.ok(classroomService.create(request));
	}

	@GetMapping
	@PreAuthorize("hasAnyRole('ADMIN','PROFESSOR')")
	public ResponseEntity<List<ClassroomResponse>> list() {
		return ResponseEntity.ok(classroomService.list());
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN','PROFESSOR')")
	public ResponseEntity<ClassroomResponse> get(@PathVariable UUID id) {
		return ResponseEntity.ok(classroomService.get(id));
	}

	@GetMapping("/{id}/students")
	@PreAuthorize("hasAnyRole('ADMIN','PROFESSOR')")
	public ResponseEntity<List<StudentResponse>> listStudents(@PathVariable UUID id) {
		return ResponseEntity.ok(studentService.listByClassroom(id));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ClassroomResponse> update(@PathVariable UUID id,
													@RequestBody @Valid ClassroomUpdateRequest request) {
		return ResponseEntity.ok(classroomService.update(id, request));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Void> delete(@PathVariable UUID id) {
		classroomService.delete(id);
		return ResponseEntity.noContent().build();
	}
}

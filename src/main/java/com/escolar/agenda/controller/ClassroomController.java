package com.escolar.agenda.controller;

import com.escolar.agenda.dto.classroom.ClassroomCreateRequest;
import com.escolar.agenda.dto.classroom.ClassroomResponse;
import com.escolar.agenda.dto.classroom.ClassroomUpdateRequest;
import com.escolar.agenda.dto.student.StudentResponse;
import com.escolar.agenda.entity.UserApp;
import com.escolar.agenda.service.ClassroomService;
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
@RequestMapping("/api/v1/classrooms")
public class ClassroomController {

	private final ClassroomService classroomService;
	private final StudentService studentService;

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ClassroomResponse> create(@RequestBody @Valid ClassroomCreateRequest request,
													Authentication authentication) {
		UserApp logged = (UserApp) authentication.getPrincipal();
		return ResponseEntity.ok(classroomService.create(request, logged));
	}

	@GetMapping
	@PreAuthorize("hasAnyRole('ADMIN','PROFESSOR')")
	public ResponseEntity<List<ClassroomResponse>> list(Authentication authentication) {
		UserApp logged = (UserApp) authentication.getPrincipal();
		return ResponseEntity.ok(classroomService.list(logged));
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN','PROFESSOR')")
	public ResponseEntity<ClassroomResponse> get(@PathVariable UUID id, Authentication authentication) {
		UserApp logged = (UserApp) authentication.getPrincipal();
		return ResponseEntity.ok(classroomService.get(id, logged));
	}

	@GetMapping("/{id}/students")
	@PreAuthorize("hasAnyRole('ADMIN','PROFESSOR')")
	public ResponseEntity<List<StudentResponse>> listStudents(@PathVariable UUID id, Authentication authentication) {
		UserApp logged = (UserApp) authentication.getPrincipal();
		return ResponseEntity.ok(studentService.listByClassroom(id, logged));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ClassroomResponse> update(@PathVariable UUID id,
													@RequestBody @Valid ClassroomUpdateRequest request,
													Authentication authentication) {
		UserApp logged = (UserApp) authentication.getPrincipal();
		return ResponseEntity.ok(classroomService.update(id, request, logged));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Void> delete(@PathVariable UUID id, Authentication authentication) {
		UserApp logged = (UserApp) authentication.getPrincipal();
		classroomService.delete(id, logged);
		return ResponseEntity.noContent().build();
	}
}

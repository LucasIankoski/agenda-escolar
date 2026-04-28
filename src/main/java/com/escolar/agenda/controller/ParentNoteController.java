package com.escolar.agenda.controller;

import com.escolar.agenda.dto.student.ParentNoteCreateRequest;
import com.escolar.agenda.dto.student.ParentNoteResponse;
import com.escolar.agenda.entity.UserApp;
import com.escolar.agenda.service.ParentNoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
@RequestMapping("/api/v1/students/{studentId}/parent-notes")
public class ParentNoteController {

	private final ParentNoteService parentNoteService;

	@PostMapping
	@PreAuthorize("hasRole('PAI')")
	public ResponseEntity<ParentNoteResponse> create(@PathVariable UUID studentId,
													 @RequestBody @Valid ParentNoteCreateRequest request,
													 Authentication authentication) {
		UserApp logged = (UserApp) authentication.getPrincipal();
		return ResponseEntity.ok(parentNoteService.create(studentId, request, logged));
	}

	@GetMapping
	@PreAuthorize("hasAnyRole('ADMIN','PROFESSOR','PAI')")
	public ResponseEntity<List<ParentNoteResponse>> listByStudent(@PathVariable UUID studentId,
																  Authentication authentication) {
		UserApp logged = (UserApp) authentication.getPrincipal();
		return ResponseEntity.ok(parentNoteService.listByStudent(studentId, logged));
	}

	@PostMapping("/mark-read")
	@PreAuthorize("hasAnyRole('ADMIN','PROFESSOR')")
	public ResponseEntity<Void> markAllAsRead(@PathVariable UUID studentId, Authentication authentication) {
		UserApp logged = (UserApp) authentication.getPrincipal();
		parentNoteService.markAllAsRead(studentId, logged);
		return ResponseEntity.noContent().build();
	}
}

package com.escolar.agenda.controller;

import com.escolar.agenda.dto.diary.DiaryCreateRequest;
import com.escolar.agenda.dto.diary.DiaryResponse;
import com.escolar.agenda.entity.UserApp;
import com.escolar.agenda.service.DiaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/diaries")
public class DiaryController {

	private final DiaryService diaryService;

	@PostMapping
	public ResponseEntity<DiaryResponse> create(@RequestBody @Valid DiaryCreateRequest req,
												Authentication authentication) {
		UserApp logged = (UserApp) authentication.getPrincipal();
		return ResponseEntity.ok(diaryService.create(req, logged));
	}

	@GetMapping("/student/{studentId}")
	public ResponseEntity<Page<DiaryResponse>> listByStudent(@PathVariable UUID studentId,
															 @RequestParam(defaultValue = "0") int page,
															 @RequestParam(defaultValue = "10") int size,
															 Authentication authentication) {
		UserApp logged = (UserApp) authentication.getPrincipal();
		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
		return ResponseEntity.ok(diaryService.listByStudent(studentId, pageable, logged));
	}

	@GetMapping("/{diaryId}")
	public ResponseEntity<DiaryResponse> get(@PathVariable UUID diaryId,
											 Authentication authentication) {
		UserApp logged = (UserApp) authentication.getPrincipal();
		return ResponseEntity.ok(diaryService.get(diaryId, logged));
	}
}

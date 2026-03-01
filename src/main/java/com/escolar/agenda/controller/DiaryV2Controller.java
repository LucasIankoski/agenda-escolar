package com.escolar.agenda.controller;

import com.escolar.agenda.dto.diary.v2.DiaryCreateV2Request;
import com.escolar.agenda.dto.diary.v2.DiaryResponseV2;
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
@RequestMapping("/api/v2/diaries")
public class DiaryV2Controller {

	private final DiaryService diaryService;

	@PostMapping
	public ResponseEntity<DiaryResponseV2> create(@RequestBody @Valid DiaryCreateV2Request req,
												  Authentication authentication) {
		UserApp logged = (UserApp) authentication.getPrincipal();
		return ResponseEntity.ok(diaryService.createV2(req, logged));
	}

	@GetMapping("/student/{studentId}")
	public ResponseEntity<Page<DiaryResponseV2>> listByStudent(@PathVariable UUID studentId,
															   @RequestParam(defaultValue = "0") int page,
															   @RequestParam(defaultValue = "10") int size,
															   Authentication authentication) {
		UserApp logged = (UserApp) authentication.getPrincipal();
		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
		return ResponseEntity.ok(diaryService.listByStudentV2(studentId, pageable, logged));
	}

	@GetMapping("/{diaryId}")
	public ResponseEntity<DiaryResponseV2> get(@PathVariable UUID diaryId,
											   Authentication authentication) {
		UserApp logged = (UserApp) authentication.getPrincipal();
		return ResponseEntity.ok(diaryService.getV2(diaryId, logged));
	}
}

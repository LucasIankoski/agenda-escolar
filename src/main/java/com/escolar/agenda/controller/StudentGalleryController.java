package com.escolar.agenda.controller;

import com.escolar.agenda.dto.student.StudentGalleryPhotoResponse;
import com.escolar.agenda.entity.StudentGalleryPhoto;
import com.escolar.agenda.entity.UserApp;
import com.escolar.agenda.service.StudentGalleryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/students/{studentId}/gallery")
public class StudentGalleryController {

	private final StudentGalleryService studentGalleryService;

	@GetMapping
	@PreAuthorize("hasAnyRole('ADMIN','PROFESSOR','PAI')")
	public ResponseEntity<List<StudentGalleryPhotoResponse>> listByStudent(@PathVariable UUID studentId,
																		   Authentication authentication) {
		UserApp logged = (UserApp) authentication.getPrincipal();
		List<StudentGalleryPhotoResponse> response = studentGalleryService.listByStudent(studentId, logged)
				.stream()
				.map(photo -> toResponse(studentId, photo))
				.toList();
		return ResponseEntity.ok(response);
	}

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@PreAuthorize("hasAnyRole('ADMIN','PROFESSOR')")
	public ResponseEntity<List<StudentGalleryPhotoResponse>> create(@PathVariable UUID studentId,
																	 @RequestParam(value = "caption", required = false) String caption,
																	 @RequestPart("files") List<MultipartFile> files,
																	 Authentication authentication) {
		UserApp logged = (UserApp) authentication.getPrincipal();
		List<StudentGalleryPhotoResponse> response = studentGalleryService.create(studentId, caption, files, logged)
				.stream()
				.map(photo -> toResponse(studentId, photo))
				.toList();
		return ResponseEntity.ok(response);
	}

	@GetMapping("/{photoId}/content")
	@PreAuthorize("hasAnyRole('ADMIN','PROFESSOR','PAI')")
	public ResponseEntity<?> getContent(@PathVariable UUID studentId,
										 @PathVariable UUID photoId,
										 Authentication authentication) {
		UserApp logged = (UserApp) authentication.getPrincipal();
		return toBinaryResponse(studentGalleryService.loadOriginal(studentId, photoId, logged));
	}

	@GetMapping("/{photoId}/thumbnail")
	@PreAuthorize("hasAnyRole('ADMIN','PROFESSOR','PAI')")
	public ResponseEntity<?> getThumbnail(@PathVariable UUID studentId,
										   @PathVariable UUID photoId,
										   Authentication authentication) {
		UserApp logged = (UserApp) authentication.getPrincipal();
		return toBinaryResponse(studentGalleryService.loadThumbnail(studentId, photoId, logged));
	}

	private ResponseEntity<?> toBinaryResponse(StudentGalleryService.GalleryBinary binary) {
		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType(binary.contentType()))
				.cacheControl(CacheControl.maxAge(Duration.ofHours(12)).cachePrivate().mustRevalidate())
				.header(HttpHeaders.CONTENT_DISPOSITION,
						ContentDisposition.inline().filename(binary.fileName()).build().toString())
				.contentLength(binary.contentLength())
				.body(binary.resource());
	}

	private StudentGalleryPhotoResponse toResponse(UUID studentId, StudentGalleryPhoto photo) {
		String imageUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
				.path("/api/v1/students/{studentId}/gallery/{photoId}/content")
				.buildAndExpand(studentId, photo.getId())
				.toUriString();

		String thumbnailUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
				.path("/api/v1/students/{studentId}/gallery/{photoId}/thumbnail")
				.buildAndExpand(studentId, photo.getId())
				.toUriString();

		return new StudentGalleryPhotoResponse(
				photo.getId(),
				photo.getStudent().getId(),
				photo.getCreatedBy().getId(),
				photo.getCreatedBy().getName(),
				photo.getCaption(),
				photo.getCreatedAt(),
				photo.getSizeInBytes(),
				photo.getWidth(),
				photo.getHeight(),
				imageUrl,
				thumbnailUrl
		);
	}
}

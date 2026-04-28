package com.escolar.agenda.service;

import com.escolar.agenda.dto.student.ParentNoteCreateRequest;
import com.escolar.agenda.dto.student.ParentNoteResponse;
import com.escolar.agenda.entity.ParentNote;
import com.escolar.agenda.entity.Student;
import com.escolar.agenda.entity.UserApp;
import com.escolar.agenda.enums.UserType;
import com.escolar.agenda.repository.ParentNoteRepository;
import com.escolar.agenda.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ParentNoteService {

	private final ParentNoteRepository parentNoteRepository;
	private final StudentRepository studentRepository;

	@Transactional
	public ParentNoteResponse create(UUID studentId, ParentNoteCreateRequest request, UserApp loggedUser) {
		validateParent(loggedUser);

		Student student = getStudentOrThrow(studentId, loggedUser);
		validateParentAccess(student, loggedUser);

		ParentNote note = new ParentNote();
		note.setSchool(loggedUser.getSchool());
		note.setStudent(student);
		note.setCreatedBy(loggedUser);
		note.setMessage(request.message().trim());

		return toResponse(parentNoteRepository.save(note));
	}

	@Transactional(readOnly = true)
	public List<ParentNoteResponse> listByStudent(UUID studentId, UserApp loggedUser) {
		Student student = getStudentOrThrow(studentId, loggedUser);
		validateStudentAccess(student, loggedUser);

		return parentNoteRepository
				.findAllBySchoolIdAndStudentIdOrderByCreatedAtDesc(loggedUser.getSchool().getId(), studentId)
				.stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional
	public void markAllAsRead(UUID studentId, UserApp loggedUser) {
		validateTeacherOrAdmin(loggedUser);
		getStudentOrThrow(studentId, loggedUser);

		List<ParentNote> pendingNotes = parentNoteRepository
				.findAllBySchoolIdAndStudentIdAndReadFalseOrderByCreatedAtDesc(
						loggedUser.getSchool().getId(),
						studentId
				);
		if (pendingNotes.isEmpty()) {
			return;
		}

		LocalDateTime readAt = LocalDateTime.now();
		for (ParentNote note : pendingNotes) {
			note.setRead(true);
			note.setReadAt(readAt);
			note.setReadBy(loggedUser);
		}

		parentNoteRepository.saveAll(pendingNotes);
	}

	private ParentNoteResponse toResponse(ParentNote note) {
		return new ParentNoteResponse(
				note.getId(),
				note.getStudent().getId(),
				note.getCreatedBy().getId(),
				note.getCreatedBy().getName(),
				note.getMessage(),
				note.getCreatedAt(),
				note.isRead(),
				note.getReadAt(),
				note.getReadBy() != null ? note.getReadBy().getId() : null
		);
	}

	private Student getStudentOrThrow(UUID studentId, UserApp loggedUser) {
		return studentRepository.findByIdAndSchoolId(studentId, loggedUser.getSchool().getId())
				.orElseThrow(() -> new NoSuchElementException("Aluno nao encontrado"));
	}

	private void validateStudentAccess(Student student, UserApp loggedUser) {
		if (isTeacherOrAdmin(loggedUser)) {
			return;
		}

		validateParentAccess(student, loggedUser);
	}

	private void validateParentAccess(Student student, UserApp loggedUser) {
		if (isParent(loggedUser)
				&& student.getParentUser() != null
				&& student.getParentUser().getId().equals(loggedUser.getId())) {
			return;
		}

		throw new AccessDeniedException("Voce nao tem acesso a este aluno.");
	}

	private void validateParent(UserApp loggedUser) {
		if (!isParent(loggedUser)) {
			throw new AccessDeniedException("Apenas responsaveis podem enviar recados.");
		}
	}

	private void validateTeacherOrAdmin(UserApp loggedUser) {
		if (!isTeacherOrAdmin(loggedUser)) {
			throw new AccessDeniedException("Apenas professor ou admin pode marcar recados como lidos.");
		}
	}

	private boolean isParent(UserApp user) {
		return user.getType() == UserType.PAI;
	}

	private boolean isTeacherOrAdmin(UserApp user) {
		return user.getType() == UserType.PROFESSOR || user.getType() == UserType.ADMIN;
	}
}

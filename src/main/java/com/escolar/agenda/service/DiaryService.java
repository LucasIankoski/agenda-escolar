package com.escolar.agenda.service;

import com.escolar.agenda.dto.diary.DiaryCreateRequest;
import com.escolar.agenda.dto.diary.DiaryResponse;
import com.escolar.agenda.entity.Diary;
import com.escolar.agenda.entity.Student;
import com.escolar.agenda.entity.UserApp;
import com.escolar.agenda.enums.UserType;
import com.escolar.agenda.repository.DiaryRepository;
import com.escolar.agenda.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DiaryService {

	private final DiaryRepository diaryRepository;
	private final StudentRepository studentRepository;

	/**
	 * Criação: somente PROFESSOR (ou ADMIN).
	 */
	@Transactional
	public DiaryResponse create(DiaryCreateRequest req, UserApp loggedUser) {

		if (!isProfessorOrAdmin(loggedUser)) {
			throw new AccessDeniedException("Apenas PROFESSOR/ADMIN pode criar diário.");
		}

		Student student = studentRepository.findById(req.studentId())
				.orElseThrow(() -> new NoSuchElementException("Aluno não encontrado"));

		Diary d = new Diary();
		d.setStudent(student);
		d.setCreatedBy(loggedUser);

		d.setFoodLevel(req.foodLevel());
		d.setFoodNotes(req.foodNotes());

		d.setSleepStart(req.sleepStart());
		d.setSleepEnd(req.sleepEnd());

		d.setHygieneType(req.hygieneType());
		d.setHygieneCount(req.hygieneCount());
		d.setPee(req.pee());
		d.setPoop(req.poop());
		d.setStoolAspect(req.stoolAspect());
		d.setHygieneNotes(req.hygieneNotes());

		d.setMood(req.mood());
		d.setActivities(req.activities());

		d = diaryRepository.save(d);
		return toResponse(d);
	}

	/**
	 * Listagem por aluno:
	 * - PROFESSOR/ADMIN pode ver
	 * - PAI pode ver apenas se for o responsável do aluno (student.parentUser)
	 */
	public Page<DiaryResponse> listByStudent(UUID studentId, Pageable pageable, UserApp loggedUser) {

		Student student = studentRepository.findById(studentId)
				.orElseThrow(() -> new NoSuchElementException("Aluno não encontrado"));

		if (isPai(loggedUser)) {
			if (student.getParentUser() == null || !student.getParentUser().getId().equals(loggedUser.getId())) {
				throw new AccessDeniedException("Você não tem acesso a este aluno.");
			}
		}

		return diaryRepository.findByStudentId(studentId, pageable).map(this::toResponse);
	}

	/**
	 * Get por id:
	 * Se PAI acessar, marca como lido automaticamente.
	 */
	@Transactional
	public DiaryResponse get(UUID diaryId, UserApp loggedUser) {

		Diary d = diaryRepository.findById(diaryId)
				.orElseThrow(() -> new NoSuchElementException("Diário não encontrado"));

		Student student = d.getStudent();

		if (isPai(loggedUser)) {
			// garante que é o pai responsável do aluno
			if (student.getParentUser() == null || !student.getParentUser().getId().equals(loggedUser.getId())) {
				throw new AccessDeniedException("Você não tem acesso a este diário.");
			}

			// marca como lido se ainda não lido
			if (!d.isRead()) {
				d.setRead(true);
				d.setReadAt(LocalDateTime.now());
				d.setReadBy(loggedUser);
				d = diaryRepository.save(d);
			}
		}

		// PROFESSOR/ADMIN podem acessar sem marcação de leitura
		return toResponse(d);
	}

	private boolean isPai(UserApp u) {
		return u.getType() == UserType.PAI;
	}

	private boolean isProfessorOrAdmin(UserApp u) {
		return u.getType() == UserType.PROFESSOR || u.getType() == UserType.ADMIN;
	}

	private DiaryResponse toResponse(Diary d) {
		return new DiaryResponse(
				d.getId(),
				d.getStudent().getId(),

				d.getCreatedBy().getId(),
				d.getCreatedBy().getName(), // ajuste: getNome() se for Usuario
				d.getCreatedAt(),

				d.getFoodLevel(),
				d.getFoodNotes(),

				d.getSleepStart(),
				d.getSleepEnd(),

				d.getHygieneType(),
				d.getHygieneCount(),
				d.getPee(),
				d.getPoop(),
				d.getStoolAspect(),
				d.getHygieneNotes(),

				d.getMood(),
				d.getActivities(),

				d.isRead(),
				d.getReadAt(),
				d.getReadBy() != null ? d.getReadBy().getId() : null
		);
	}
}

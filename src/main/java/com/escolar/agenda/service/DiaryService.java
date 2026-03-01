package com.escolar.agenda.service;

import com.escolar.agenda.dto.diary.DiaryCreateRequest;
import com.escolar.agenda.dto.diary.DiaryResponse;
import com.escolar.agenda.dto.diary.v2.DiaryCreateV2Request;
import com.escolar.agenda.dto.diary.v2.DiaryResponseV2;
import com.escolar.agenda.dto.diary.v2.DiaryV2Payload;
import com.escolar.agenda.entity.Diary;
import com.escolar.agenda.entity.Student;
import com.escolar.agenda.entity.UserApp;
import com.escolar.agenda.enums.DiaryVersion;
import com.escolar.agenda.enums.FoodLevel;
import com.escolar.agenda.enums.Mood;
import com.escolar.agenda.enums.UserType;
import com.escolar.agenda.repository.DiaryRepository;
import com.escolar.agenda.repository.StudentRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringJoiner;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DiaryService {

	private final DiaryRepository diaryRepository;
	private final StudentRepository studentRepository;
	private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

	/**
	 * CriaÃ§Ã£o: somente PROFESSOR (ou ADMIN).
	 */
	@Transactional
	public DiaryResponse create(DiaryCreateRequest req, UserApp loggedUser) {
		validateProfessorOrAdmin(loggedUser);

		Student student = getStudentOrThrow(req.studentId());

		Diary d = new Diary();
		d.setStudent(student);
		d.setCreatedBy(loggedUser);
		d.setDiaryVersion(DiaryVersion.V1);

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

	@Transactional
	public DiaryResponseV2 createV2(DiaryCreateV2Request req, UserApp loggedUser) {
		validateProfessorOrAdmin(loggedUser);

		Student student = getStudentOrThrow(req.studentId());
		DiaryV2Payload payload = req.payload();

		Diary d = new Diary();
		d.setStudent(student);
		d.setCreatedBy(loggedUser);
		d.setDiaryVersion(DiaryVersion.V2);
		d.setV2Payload(writePayload(payload));

		// Preenche os campos legados com um resumo para manter compatibilidade com a v1.
		d.setFoodLevel(deriveLegacyFoodLevel(payload));
		d.setFoodNotes(buildFoodSummary(payload.meals()));
		d.setSleepStart(deriveSleepStart(payload.sleep()));
		d.setSleepEnd(deriveSleepEnd(payload.sleep()));
		d.setPee(isSelected(payload.needs().pee()));
		d.setPoop(isSelected(payload.needs().poop()));
		d.setHygieneCount(sumCounts(payload.needs()));
		d.setMood(Mood.TRANQUILA);
		d.setActivities(buildActivitiesSummary(payload));

		d = diaryRepository.save(d);
		return toV2Response(d);
	}

	/**
	 * Listagem por aluno:
	 * - PROFESSOR/ADMIN pode ver
	 * - PAI pode ver apenas se for o responsÃ¡vel do aluno (student.parentUser)
	 */
	public Page<DiaryResponse> listByStudent(UUID studentId, Pageable pageable, UserApp loggedUser) {

		Student student = getStudentOrThrow(studentId);
		validateStudentAccess(student, loggedUser);

		return diaryRepository.findByStudentId(studentId, pageable).map(this::toResponse);
	}

	public Page<DiaryResponseV2> listByStudentV2(UUID studentId, Pageable pageable, UserApp loggedUser) {
		Student student = getStudentOrThrow(studentId);
		validateStudentAccess(student, loggedUser);

		return diaryRepository.findByStudentIdAndDiaryVersion(studentId, DiaryVersion.V2, pageable)
				.map(this::toV2Response);
	}

	/**
	 * Get por id:
	 * Se PAI acessar, marca como lido automaticamente.
	 */
	@Transactional
	public DiaryResponse get(UUID diaryId, UserApp loggedUser) {

		Diary d = getDiaryOrThrow(diaryId);
		markAsReadWhenParent(d, loggedUser);

		return toResponse(d);
	}

	@Transactional
	public DiaryResponseV2 getV2(UUID diaryId, UserApp loggedUser) {
		Diary d = getDiaryOrThrow(diaryId);

		if (d.getDiaryVersion() != DiaryVersion.V2) {
			throw new NoSuchElementException("Diário não encontrado");
		}

		d = markAsReadWhenParent(d, loggedUser);
		return toV2Response(d);
	}

	private Student getStudentOrThrow(UUID studentId) {
		return studentRepository.findById(studentId)
				.orElseThrow(() -> new NoSuchElementException("Aluno não encontrado"));
	}

	private Diary getDiaryOrThrow(UUID diaryId) {
		return diaryRepository.findById(diaryId)
				.orElseThrow(() -> new NoSuchElementException("Diário não encontrado"));
	}

	private void validateStudentAccess(Student student, UserApp loggedUser) {
		if (isPai(loggedUser)) {
			if (student.getParentUser() == null || !student.getParentUser().getId().equals(loggedUser.getId())) {
				throw new AccessDeniedException("Você não tem acesso a este aluno.");
			}
		}
	}

	private Diary markAsReadWhenParent(Diary d, UserApp loggedUser) {
		Student student = d.getStudent();

		if (isPai(loggedUser)) {
			if (student.getParentUser() == null || !student.getParentUser().getId().equals(loggedUser.getId())) {
				throw new AccessDeniedException("VocÃª nÃ£o tem acesso a este diÃ¡rio.");
			}

			if (!d.isRead()) {
				d.setRead(true);
				d.setReadAt(LocalDateTime.now());
				d.setReadBy(loggedUser);
				return diaryRepository.save(d);
			}
		}

		return d;
	}

	private void validateProfessorOrAdmin(UserApp u) {
		if (!isProfessorOrAdmin(u)) {
			throw new AccessDeniedException("Apenas PROFESSOR/ADMIN pode criar diário.");
		}
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
				d.getCreatedBy().getName(),
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

	private DiaryResponseV2 toV2Response(Diary d) {
		return new DiaryResponseV2(
				d.getId(),
				d.getStudent().getId(),
				d.getCreatedBy().getId(),
				d.getCreatedBy().getName(),
				d.getCreatedAt(),
				readPayload(d.getV2Payload()),
				d.isRead(),
				d.getReadAt(),
				d.getReadBy() != null ? d.getReadBy().getId() : null
		);
	}

	private String writePayload(DiaryV2Payload payload) {
		try {
			return objectMapper.writeValueAsString(payload);
		} catch (JsonProcessingException e) {
			throw new IllegalStateException("Não foi possível serializar o payload v2 do diário", e);
		}
	}

	private DiaryV2Payload readPayload(String payload) {
		if (payload == null || payload.isBlank()) {
			throw new IllegalStateException("DiÃ¡rio v2 sem payload");
		}

		try {
			return objectMapper.readValue(payload, DiaryV2Payload.class);
		} catch (JsonProcessingException e) {
			throw new IllegalStateException("NÃo foi possível ler o payload v2 do diário", e);
		}
	}

	private FoodLevel deriveLegacyFoodLevel(DiaryV2Payload payload) {
		List<DiaryV2Payload.MealStatus> statuses = new ArrayList<>();
		DiaryV2Payload.Meals meals = payload.meals();

		if (meals.breakfast() != null) statuses.add(meals.breakfast());
		if (meals.lunch() != null) statuses.add(meals.lunch());
		if (meals.bottle() != null) statuses.add(meals.bottle());
		if (meals.fruit() != null) statuses.add(meals.fruit());
		if (meals.dinner() != null) statuses.add(meals.dinner());
		if (meals.supper() != null) statuses.add(meals.supper());

		if (statuses.contains(DiaryV2Payload.MealStatus.RECUSOU)) {
			return FoodLevel.RECUSOU;
		}
		if (statuses.contains(DiaryV2Payload.MealStatus.MENOS_DA_METADE)) {
			return FoodLevel.VAZIO;
		}
		if (statuses.contains(DiaryV2Payload.MealStatus.METADE)) {
			return FoodLevel.METADE;
		}
		if (statuses.contains(DiaryV2Payload.MealStatus.BEM)) {
			return FoodLevel.CHEIO;
		}
		return FoodLevel.METADE;
	}

	private String buildFoodSummary(DiaryV2Payload.Meals meals) {
		StringJoiner joiner = new StringJoiner("; ");
		appendMeal(joiner, "Café da manha", meals.breakfast());
		appendMeal(joiner, "Almoço", meals.lunch());
		appendMeal(joiner, "Mamadeira", meals.bottle());
		appendMeal(joiner, "Fruta", meals.fruit());
		appendMeal(joiner, "Janta", meals.dinner());
		appendMeal(joiner, "Ceia", meals.supper());
		return joiner.length() == 0 ? null : joiner.toString();
	}

	private void appendMeal(StringJoiner joiner, String label, DiaryV2Payload.MealStatus status) {
		if (status != null) {
			joiner.add(label + "=" + status.name());
		}
	}

	private LocalDateTime deriveSleepStart(DiaryV2Payload.Sleep sleep) {
		LocalTime morning = extractStart(sleep.morning());
		LocalTime afternoon = extractStart(sleep.afternoon());
		LocalTime start = morning;

		if (start == null || (afternoon != null && afternoon.isBefore(start))) {
			start = afternoon;
		}

		return start != null ? LocalDateTime.of(LocalDate.now(), start) : null;
	}

	private LocalDateTime deriveSleepEnd(DiaryV2Payload.Sleep sleep) {
		LocalTime morning = extractEnd(sleep.morning());
		LocalTime afternoon = extractEnd(sleep.afternoon());
		LocalTime end = morning;

		if (end == null || (afternoon != null && afternoon.isAfter(end))) {
			end = afternoon;
		}

		return end != null ? LocalDateTime.of(LocalDate.now(), end) : null;
	}

	private LocalTime extractStart(DiaryV2Payload.SleepPeriod period) {
		if (period != null && period.slept()) {
			return period.startTime();
		}
		return null;
	}

	private LocalTime extractEnd(DiaryV2Payload.SleepPeriod period) {
		if (period != null && period.slept()) {
			return period.endTime();
		}
		return null;
	}

	private boolean isSelected(DiaryV2Payload.CountableItem item) {
		return item != null && item.selected();
	}

	private int sumCounts(DiaryV2Payload.Needs needs) {
		return safeCount(needs.pee()) + safeCount(needs.poop());
	}

	private int safeCount(DiaryV2Payload.CountableItem item) {
		return item != null && item.count() != null ? item.count() : 0;
	}

	private String buildActivitiesSummary(DiaryV2Payload payload) {
		List<String> activities = new ArrayList<>();
		DiaryV2Payload.PedagogicalProposals proposals = payload.pedagogicalProposals();

		if (proposals.pedagogicalActivity()) {
			activities.add("Ativ. pedagógica");
		}
		if (proposals.music()) {
			activities.add("Música");
		}
		if (proposals.patio()) {
			activities.add("Pátio");
		}
		if (proposals.freePlay()) {
			activities.add("Brincadeira livre");
		}

		if (payload.teacherNote() != null && !payload.teacherNote().isBlank()) {
			activities.add("Recado: " + payload.teacherNote().trim());
		}

		return activities.isEmpty() ? null : String.join(", ", activities);
	}
}

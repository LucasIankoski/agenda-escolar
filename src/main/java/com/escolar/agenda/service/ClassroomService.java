package com.escolar.agenda.service;

import com.escolar.agenda.dto.classroom.ClassroomCreateRequest;
import com.escolar.agenda.dto.classroom.ClassroomResponse;
import com.escolar.agenda.dto.classroom.ClassroomUpdateRequest;
import com.escolar.agenda.entity.Classroom;
import com.escolar.agenda.entity.UserApp;
import com.escolar.agenda.repository.ClassroomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClassroomService {

	private final ClassroomRepository classroomRepository;

	public ClassroomResponse create(ClassroomCreateRequest request, UserApp loggedUser) {
		UUID schoolId = loggedUser.getSchool().getId();
		if (classroomRepository.existsBySchoolIdAndName(schoolId, request.name())) {
			throw new IllegalArgumentException("Ja existe uma turma com esse nome nesta escola");
		}

		Classroom c = new Classroom();
		c.setSchool(loggedUser.getSchool());
		c.setName(request.name());
		c.setActive(true);
		return toResponse(classroomRepository.save(c));
	}

	public ClassroomResponse update(UUID id, ClassroomUpdateRequest request, UserApp loggedUser) {
		Classroom c = getEntityOrThrow(id, loggedUser);
		UUID schoolId = loggedUser.getSchool().getId();

		if (!c.getName().equalsIgnoreCase(request.name())
				&& classroomRepository.existsBySchoolIdAndName(schoolId, request.name())) {
			throw new IllegalArgumentException("Ja existe uma turma com esse nome nesta escola");
		}

		c.setName(request.name());
		c.setActive(request.active());
		return toResponse(classroomRepository.save(c));
	}

	public ClassroomResponse get(UUID id, UserApp loggedUser) {
		return toResponse(getEntityOrThrow(id, loggedUser));
	}

	public List<ClassroomResponse> list(UserApp loggedUser) {
		return classroomRepository.findAllBySchoolId(loggedUser.getSchool().getId()).stream()
				.map(this::toResponse)
				.toList();
	}

	public void delete(UUID id, UserApp loggedUser) {
		Classroom c = getEntityOrThrow(id, loggedUser);
		classroomRepository.delete(c);
	}

	public Classroom getEntityOrThrow(UUID id, UserApp loggedUser) {
		return classroomRepository.findByIdAndSchoolId(id, loggedUser.getSchool().getId())
				.orElseThrow(() -> new NoSuchElementException("Turma nao encontrada"));
	}

	private ClassroomResponse toResponse(Classroom c) {
		return new ClassroomResponse(c.getId(), c.getName(), c.isActive());
	}
}

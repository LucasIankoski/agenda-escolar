package com.escolar.agenda.service;

import com.escolar.agenda.dto.classroom.ClassroomCreateRequest;
import com.escolar.agenda.dto.classroom.ClassroomResponse;
import com.escolar.agenda.dto.classroom.ClassroomUpdateRequest;
import com.escolar.agenda.entity.Classroom;
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

	public ClassroomResponse create(ClassroomCreateRequest request) {
		if (classroomRepository.existsByName(request.name())) {
			throw new IllegalArgumentException("Já existe uma turma com esse nome");
		}
		Classroom c = new Classroom();
		c.setName(request.name());
		c.setActive(true);
		return toResponse(classroomRepository.save(c));
	}

	public ClassroomResponse update(UUID id, ClassroomUpdateRequest request) {
		Classroom c = getEntityOrThrow(id);

		// se renomear, valida duplicidade
		if (!c.getName().equalsIgnoreCase(request.name()) && classroomRepository.existsByName(request.name())) {
			throw new IllegalArgumentException("Já existe uma turma com esse nome");
		}

		c.setName(request.name());
		c.setActive(request.active());
		return toResponse(classroomRepository.save(c));
	}

	public ClassroomResponse get(UUID id) {
		return toResponse(getEntityOrThrow(id));
	}

	public List<ClassroomResponse> list() {
		return classroomRepository.findAll().stream().map(this::toResponse).toList();
	}

	public void delete(UUID id) {
		Classroom c = getEntityOrThrow(id);
		classroomRepository.delete(c);
	}

	public Classroom getEntityOrThrow(UUID id) {
		return classroomRepository.findById(id)
				.orElseThrow(() -> new NoSuchElementException("Turma não encontrada"));
	}

	private ClassroomResponse toResponse(Classroom c) {
		return new ClassroomResponse(c.getId(), c.getName(), c.isActive());
	}
}

package com.escolar.agenda.repository;

import com.escolar.agenda.entity.Classroom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ClassroomRepository extends JpaRepository<Classroom, UUID> {
	Optional<Classroom> findByIdAndSchoolId(UUID id, UUID schoolId);

	Optional<Classroom> findBySchoolIdAndName(UUID schoolId, String name);

	boolean existsBySchoolIdAndName(UUID schoolId, String name);

	java.util.List<Classroom> findAllBySchoolId(UUID schoolId);
}

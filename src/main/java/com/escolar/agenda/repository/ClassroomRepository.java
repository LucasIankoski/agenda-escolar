package com.escolar.agenda.repository;

import com.escolar.agenda.entity.Classroom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ClassroomRepository extends JpaRepository<Classroom, UUID> {
	Optional<Classroom> findByName(String name);
	boolean existsByName(String name);
}
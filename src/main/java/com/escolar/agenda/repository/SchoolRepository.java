package com.escolar.agenda.repository;

import com.escolar.agenda.entity.School;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SchoolRepository extends JpaRepository<School, UUID> {

	Optional<School> findBySlug(String slug);

	boolean existsBySlug(String slug);

	List<School> findAllByOrderByNameAsc();

	List<School> findAllByActiveTrueOrderByNameAsc();
}

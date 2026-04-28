package com.escolar.agenda.repository;

import com.escolar.agenda.entity.UserApp;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserApp, UUID> {
	@EntityGraph(attributePaths = "school")
	Optional<UserApp> findBySchoolIdAndEmail(UUID schoolId, String email);

	@EntityGraph(attributePaths = "school")
	Optional<UserApp> findByIdAndSchoolId(UUID id, UUID schoolId);

	@EntityGraph(attributePaths = "school")
	Optional<UserApp> findBySchoolSlugAndEmail(String schoolSlug, String email);

	@EntityGraph(attributePaths = "school")
	Optional<UserApp> findBySchoolIsNullAndEmail(String email);

	Optional<UserApp> findByIdAndSchoolIsNull(UUID id);

	boolean existsBySchoolIdAndEmail(UUID schoolId, String email);

	boolean existsBySchoolIsNullAndEmail(String email);

	@EntityGraph(attributePaths = "school")
	java.util.List<UserApp> findAllBySchoolId(UUID schoolId);
}


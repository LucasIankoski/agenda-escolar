package com.escolar.agenda.repository;

import com.escolar.agenda.entity.UserApp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserApp, UUID> {
	Optional<UserApp> findByEmail(String email);

	boolean existsByEmail(String email);
}


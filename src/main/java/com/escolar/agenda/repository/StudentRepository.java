package com.escolar.agenda.repository;

import com.escolar.agenda.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StudentRepository extends JpaRepository<Student, UUID> {
	List<Student> findAllByClassroomId(UUID classroomId);
	List<Student> findAllByParentUserId(UUID parentUserId);

	Optional<Student> findByResponsibleContact(String responsibleContact);

	Optional<Student> findByParentUserEmail(String email);
}

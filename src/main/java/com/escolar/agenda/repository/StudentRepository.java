package com.escolar.agenda.repository;

import com.escolar.agenda.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface StudentRepository extends JpaRepository<Student, UUID> {
	List<Student> findAllByClassroomId(UUID classroomId);
}

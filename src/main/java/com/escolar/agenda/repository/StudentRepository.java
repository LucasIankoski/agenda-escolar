package com.escolar.agenda.repository;

import com.escolar.agenda.entity.Student;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StudentRepository extends JpaRepository<Student, UUID> {
	@EntityGraph(attributePaths = {"classroom", "parentUser"})
	List<Student> findAllBySchoolId(UUID schoolId);

	@EntityGraph(attributePaths = {"classroom", "parentUser"})
	List<Student> findAllBySchoolIdAndClassroomId(UUID schoolId, UUID classroomId);

	@EntityGraph(attributePaths = {"classroom", "parentUser"})
	List<Student> findAllBySchoolIdAndParentUserId(UUID schoolId, UUID parentUserId);

	@EntityGraph(attributePaths = {"classroom", "parentUser"})
	Optional<Student> findByIdAndSchoolId(UUID id, UUID schoolId);

	@EntityGraph(attributePaths = {"classroom", "parentUser"})
	Optional<Student> findBySchoolIdAndResponsibleContact(UUID schoolId, String responsibleContact);

	@EntityGraph(attributePaths = {"classroom", "parentUser"})
	Optional<Student> findBySchoolIdAndParentUserEmail(UUID schoolId, String email);
}

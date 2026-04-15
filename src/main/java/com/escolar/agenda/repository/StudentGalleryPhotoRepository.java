package com.escolar.agenda.repository;

import com.escolar.agenda.entity.StudentGalleryPhoto;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentGalleryPhotoRepository extends JpaRepository<StudentGalleryPhoto, UUID> {

	@EntityGraph(attributePaths = {"student", "createdBy"})
	List<StudentGalleryPhoto> findAllByStudentIdOrderByCreatedAtDesc(UUID studentId);

	@EntityGraph(attributePaths = {"student", "createdBy"})
	Optional<StudentGalleryPhoto> findByIdAndStudentId(UUID id, UUID studentId);
}

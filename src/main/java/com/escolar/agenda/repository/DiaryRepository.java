package com.escolar.agenda.repository;

import com.escolar.agenda.entity.Diary;
import com.escolar.agenda.enums.DiaryVersion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, UUID> {

	@EntityGraph(attributePaths = {"student", "student.parentUser", "createdBy", "readBy"})
	Optional<Diary> findByIdAndSchoolId(UUID id, UUID schoolId);

	@EntityGraph(attributePaths = {"student", "student.parentUser", "createdBy", "readBy"})
	Page<Diary> findBySchoolIdAndStudentId(UUID schoolId, UUID studentId, Pageable pageable);

	@EntityGraph(attributePaths = {"student", "student.parentUser", "createdBy", "readBy"})
	Page<Diary> findBySchoolIdAndStudentIdAndDiaryVersion(
			UUID schoolId,
			UUID studentId,
			DiaryVersion diaryVersion,
			Pageable pageable
	);
}

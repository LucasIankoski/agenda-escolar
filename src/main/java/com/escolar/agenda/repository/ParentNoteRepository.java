package com.escolar.agenda.repository;

import com.escolar.agenda.entity.ParentNote;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface ParentNoteRepository extends JpaRepository<ParentNote, UUID> {

	@EntityGraph(attributePaths = {"student", "createdBy", "readBy"})
	List<ParentNote> findAllBySchoolIdAndStudentIdOrderByCreatedAtDesc(UUID schoolId, UUID studentId);

	@EntityGraph(attributePaths = {"student", "createdBy", "readBy"})
	List<ParentNote> findAllBySchoolIdAndStudentIdAndReadFalseOrderByCreatedAtDesc(UUID schoolId, UUID studentId);

	long countBySchoolIdAndStudentIdAndReadFalse(UUID schoolId, UUID studentId);

	@Query("""
			select pn.student.id as studentId, count(pn) as pendingCount
			from ParentNote pn
			where pn.read = false
			  and pn.school.id = :schoolId
			  and pn.student.id in :studentIds
			group by pn.student.id
			""")
	List<ParentNotePendingCount> countPendingBySchoolIdAndStudentIds(
			@Param("schoolId") UUID schoolId,
			@Param("studentIds") Collection<UUID> studentIds
	);

	interface ParentNotePendingCount {
		UUID getStudentId();

		long getPendingCount();
	}
}

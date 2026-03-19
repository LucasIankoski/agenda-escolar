package com.escolar.agenda.repository;

import com.escolar.agenda.entity.ParentNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface ParentNoteRepository extends JpaRepository<ParentNote, UUID> {

	List<ParentNote> findAllByStudentIdOrderByCreatedAtDesc(UUID studentId);

	List<ParentNote> findAllByStudentIdAndReadFalseOrderByCreatedAtDesc(UUID studentId);

	long countByStudentIdAndReadFalse(UUID studentId);

	@Query("""
			select pn.student.id as studentId, count(pn) as pendingCount
			from ParentNote pn
			where pn.read = false
			  and pn.student.id in :studentIds
			group by pn.student.id
			""")
	List<ParentNotePendingCount> countPendingByStudentIds(@Param("studentIds") Collection<UUID> studentIds);

	interface ParentNotePendingCount {
		UUID getStudentId();

		long getPendingCount();
	}
}

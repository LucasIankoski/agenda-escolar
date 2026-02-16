package com.escolar.agenda.repository;

import com.escolar.agenda.entity.Diary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, UUID> {

	Optional<Diary> findByStudentIdAndDate(UUID studentId, Timestamp date);
}


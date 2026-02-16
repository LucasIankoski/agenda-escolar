package com.escolar.agenda.repository;

import com.escolar.agenda.entity.Diary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, UUID> {

	Page<Diary> findByStudentId(UUID studentId, Pageable pageable);
}
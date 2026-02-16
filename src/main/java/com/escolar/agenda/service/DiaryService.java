package com.escolar.agenda.service;

import com.escolar.agenda.entity.Diary;
import com.escolar.agenda.repository.DiaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DiaryService {

	private final DiaryRepository repository;

	public Diary create(Diary diary) {
		return repository.save(diary);
	}

	public Diary find(UUID id, Timestamp date) {
		return repository.findByStudentIdAndDate(id, date)
				.orElseThrow();
	}
}


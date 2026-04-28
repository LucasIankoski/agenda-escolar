package com.escolar.agenda.service;

import com.escolar.agenda.config.TenantProperties;
import com.escolar.agenda.dto.school.SchoolLoginOptionResponse;
import com.escolar.agenda.dto.school.SchoolResponse;
import com.escolar.agenda.entity.School;
import com.escolar.agenda.repository.SchoolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class SchoolService {

	private static final Pattern DIACRITICAL_MARKS = Pattern.compile("\\p{M}+");
	private static final Pattern NON_SLUG_CHARS = Pattern.compile("[^a-z0-9]+");
	private static final Pattern TRIM_HYPHENS = Pattern.compile("(^-+|-+$)");

	private final SchoolRepository schoolRepository;
	private final TenantProperties tenantProperties;

	@Transactional(readOnly = true)
	public School getByIdOrThrow(UUID id) {
		return schoolRepository.findById(id)
				.orElseThrow(() -> new NoSuchElementException("Escola nao encontrada"));
	}

	@Transactional(readOnly = true)
	public School getBySlugOrThrow(String rawSlug) {
		String slug = normalizeSlug(rawSlug);
		return schoolRepository.findBySlug(slug)
				.filter(School::isActive)
				.orElseThrow(() -> new NoSuchElementException("Escola nao encontrada"));
	}

	@Transactional(readOnly = true)
	public List<SchoolResponse> list() {
		return schoolRepository.findAllByOrderByNameAsc().stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<SchoolLoginOptionResponse> listLoginOptions() {
		return schoolRepository.findAllByActiveTrueOrderByNameAsc().stream()
				.map(school -> new SchoolLoginOptionResponse(school.getName(), school.getSlug()))
				.toList();
	}

	@Transactional
	public School resolveForLogin(String rawSlug) {
		if (isBlank(rawSlug)) {
			return getOrCreateDefaultSchool();
		}
		return getBySlugOrThrow(rawSlug);
	}

	@Transactional
	public School resolveOrCreateForRegistration(String rawName, String rawSlug) {
		if (isBlank(rawName) && isBlank(rawSlug)) {
			return getOrCreateDefaultSchool();
		}

		String slug = normalizeSlug(isBlank(rawSlug) ? rawName : rawSlug);
		return schoolRepository.findBySlug(slug)
				.filter(School::isActive)
				.orElseGet(() -> createSchool(resolveName(rawName, slug), slug));
	}

	@Transactional
	public School getOrCreateDefaultSchool() {
		String slug = normalizeSlug(tenantProperties.getDefaultSchoolSlug());
		return schoolRepository.findBySlug(slug)
				.orElseGet(() -> createSchool(tenantProperties.getDefaultSchoolName(), slug));
	}

	@Transactional
	public School create(String rawName, String rawSlug) {
		String slug = normalizeSlug(isBlank(rawSlug) ? rawName : rawSlug);
		if (schoolRepository.existsBySlug(slug)) {
			throw new IllegalArgumentException("Ja existe uma escola com esse codigo");
		}
		return createSchool(resolveName(rawName, slug), slug);
	}

	public SchoolResponse toResponse(School school) {
		return new SchoolResponse(school.getId(), school.getName(), school.getSlug(), school.isActive());
	}

	private School createSchool(String name, String slug) {
		School school = new School();
		school.setName(name);
		school.setSlug(slug);
		school.setActive(true);
		return schoolRepository.save(school);
	}

	private String resolveName(String rawName, String slug) {
		if (!isBlank(rawName)) {
			return rawName.trim();
		}
		return slug;
	}

	public String normalizeSlug(String rawSlug) {
		if (isBlank(rawSlug)) {
			throw new IllegalArgumentException("Identificador da escola nao informado");
		}

		String normalized = Normalizer.normalize(rawSlug.trim(), Normalizer.Form.NFD).toLowerCase();
		normalized = DIACRITICAL_MARKS.matcher(normalized).replaceAll("");
		normalized = NON_SLUG_CHARS.matcher(normalized).replaceAll("-");
		normalized = TRIM_HYPHENS.matcher(normalized).replaceAll("");

		if (normalized.isBlank()) {
			throw new IllegalArgumentException("Identificador da escola invalido");
		}
		return normalized;
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}
}

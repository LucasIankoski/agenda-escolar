package com.escolar.agenda.service;

import com.escolar.agenda.dto.student.StudentCreateRequest;
import com.escolar.agenda.dto.student.StudentResponse;
import com.escolar.agenda.entity.Classroom;
import com.escolar.agenda.entity.Student;
import com.escolar.agenda.entity.UserApp;
import com.escolar.agenda.enums.UserType;
import com.escolar.agenda.repository.ClassroomRepository;
import com.escolar.agenda.repository.ParentNoteRepository;
import com.escolar.agenda.repository.StudentRepository;
import com.escolar.agenda.repository.UserRepository;
import com.escolar.agenda.util.LoginNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentService {

	private final StudentRepository studentRepository;
	private final ClassroomRepository classroomRepository;
	private final ParentNoteRepository parentNoteRepository;

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Transactional
	public StudentResponse create(StudentCreateRequest req, UserApp loggedUser) {
		UUID schoolId = loggedUser.getSchool().getId();
		Classroom classroom = classroomRepository.findByIdAndSchoolId(req.classroomId(), schoolId)
				.orElseThrow(() -> new NoSuchElementException("Turma nao encontrada"));

		String parentContactLogin = LoginNormalizer.normalize(req.parentContact());
		if (!LoginNormalizer.isBrazilianMobilePhone(parentContactLogin)) {
			throw new IllegalArgumentException("Contato do responsavel deve ser um celular valido");
		}

		if (userRepository.existsBySchoolIdAndEmail(schoolId, parentContactLogin)) {
			throw new IllegalArgumentException("Ja existe usuario com esse contato/login nesta escola");
		}

		UserApp parent = new UserApp();
		parent.setSchool(loggedUser.getSchool());
		parent.setName(buildFullName(req.parentName(), req.parentLastName()));
		parent.setEmail(parentContactLogin);
		parent.setPassword(passwordEncoder.encode("12345"));
		parent.setType(UserType.PAI);
		parent.setActive(true);
		parent = userRepository.save(parent);

		Student student = new Student();
		student.setSchool(loggedUser.getSchool());
		student.setName(req.name());
		student.setLastName(req.lastName());
		student.setResponsibleName(req.parentName().trim());
		student.setResponsibleLastName(req.parentLastName().trim());
		student.setResponsibleContact(parentContactLogin);
		student.setBirthDate(req.birthDate());
		student.setClassroom(classroom);
		student.setParentUser(parent);

		student = studentRepository.save(student);
		return toResponse(student, 0L);
	}

	public List<StudentResponse> list(UserApp loggedUser) {
		UUID schoolId = loggedUser.getSchool().getId();
		if (isParent(loggedUser)) {
			return toResponseList(studentRepository.findAllBySchoolIdAndParentUserId(schoolId, loggedUser.getId()), schoolId);
		}

		validateCanViewAllStudents(loggedUser);
		return toResponseList(studentRepository.findAllBySchoolId(schoolId), schoolId);
	}

	public List<StudentResponse> listByClassroom(UUID classroomId, UserApp loggedUser) {
		UUID schoolId = loggedUser.getSchool().getId();
		classroomRepository.findByIdAndSchoolId(classroomId, schoolId)
				.orElseThrow(() -> new NoSuchElementException("Turma nao encontrada"));

		return toResponseList(studentRepository.findAllBySchoolIdAndClassroomId(schoolId, classroomId), schoolId);
	}

	public StudentResponse get(UUID id, UserApp loggedUser) {
		Student s = getEntityOrThrow(id, loggedUser);
		validateStudentAccess(s, loggedUser);
		return toResponse(s, loadPendingNoteCount(s.getId(), loggedUser.getSchool().getId()));
	}

	public StudentResponse getByResponsible(String responsibleLogin, UserApp loggedUser) {
		UUID schoolId = loggedUser.getSchool().getId();
		String normalizedLogin;
		if (isParent(loggedUser)) {
			normalizedLogin = loggedUser.getEmail();
		} else {
			validateCanViewAllStudents(loggedUser);
			if (responsibleLogin == null || responsibleLogin.isBlank()) {
				throw new IllegalArgumentException("Login do responsavel e obrigatorio");
			}
			normalizedLogin = LoginNormalizer.normalize(responsibleLogin);
		}

		Student s = studentRepository.findBySchoolIdAndResponsibleContact(schoolId, normalizedLogin)
				.or(() -> studentRepository.findBySchoolIdAndParentUserEmail(schoolId, normalizedLogin))
				.orElseThrow(() -> new NoSuchElementException("Aluno nao encontrado para o responsavel informado"));
		return toResponse(s, loadPendingNoteCount(s.getId(), schoolId));
	}

	public void delete(UUID id, UserApp loggedUser) {
		Student s = getEntityOrThrow(id, loggedUser);
		studentRepository.delete(s);
	}

	private List<StudentResponse> toResponseList(List<Student> students, UUID schoolId) {
		Map<UUID, Long> pendingCounts = loadPendingNoteCounts(students, schoolId);
		return students.stream()
				.map(student -> toResponse(student, pendingCounts.getOrDefault(student.getId(), 0L)))
				.toList();
	}

	private Map<UUID, Long> loadPendingNoteCounts(List<Student> students, UUID schoolId) {
		List<UUID> studentIds = students.stream().map(Student::getId).toList();
		if (studentIds.isEmpty()) {
			return Map.of();
		}

		return parentNoteRepository.countPendingBySchoolIdAndStudentIds(schoolId, studentIds).stream()
				.collect(Collectors.toMap(
						ParentNoteRepository.ParentNotePendingCount::getStudentId,
						ParentNoteRepository.ParentNotePendingCount::getPendingCount
				));
	}

	private long loadPendingNoteCount(UUID studentId, UUID schoolId) {
		return parentNoteRepository.countBySchoolIdAndStudentIdAndReadFalse(schoolId, studentId);
	}

	private StudentResponse toResponse(Student s, long pendingParentNoteCount) {
		UserApp parentUser = s.getParentUser();
		UUID parentId = parentUser != null ? parentUser.getId() : null;
		String parentEmail = parentUser != null ? parentUser.getEmail() : null;

		String parentName = s.getResponsibleName();
		String parentLastName = s.getResponsibleLastName();
		String parentContact = s.getResponsibleContact();

		if (parentUser != null) {
			if (isBlank(parentContact)) {
				parentContact = parentUser.getEmail();
			}

			if (isBlank(parentName) && !isBlank(parentUser.getName())) {
				String[] parsedName = splitFullName(parentUser.getName());
				parentName = parsedName[0];
				if (isBlank(parentLastName)) {
					parentLastName = parsedName[1];
				}
			}
		}

		return new StudentResponse(
				s.getId(),
				s.getName(),
				s.getLastName(),
				s.getBirthDate(),
				s.getClassroom().getId(),
				parentId,
				parentName,
				parentLastName,
				parentContact,
				parentEmail,
				pendingParentNoteCount
		);
	}

	private Student getEntityOrThrow(UUID id, UserApp loggedUser) {
		return studentRepository.findByIdAndSchoolId(id, loggedUser.getSchool().getId())
				.orElseThrow(() -> new NoSuchElementException("Aluno nao encontrado"));
	}

	private void validateCanViewAllStudents(UserApp loggedUser) {
		if (!isAdminOrProfessor(loggedUser)) {
			throw new AccessDeniedException("Acesso negado para visualizar alunos.");
		}
	}

	private void validateStudentAccess(Student student, UserApp loggedUser) {
		if (isAdminOrProfessor(loggedUser)) {
			return;
		}

		if (isParent(loggedUser) && isLinkedParent(student, loggedUser)) {
			return;
		}

		throw new AccessDeniedException("Voce nao tem acesso a este aluno.");
	}

	private boolean isLinkedParent(Student student, UserApp loggedUser) {
		return student.getParentUser() != null && student.getParentUser().getId().equals(loggedUser.getId());
	}

	private boolean isParent(UserApp user) {
		return user.getType() == UserType.PAI;
	}

	private boolean isAdminOrProfessor(UserApp user) {
		return user.getType() == UserType.ADMIN || user.getType() == UserType.PROFESSOR;
	}

	private String buildFullName(String firstName, String lastName) {
		return firstName.trim() + " " + lastName.trim();
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}

	private String[] splitFullName(String fullName) {
		String normalized = fullName.trim().replaceAll("\\s+", " ");
		int firstSpace = normalized.indexOf(' ');
		if (firstSpace < 0) {
			return new String[]{normalized, null};
		}

		String firstName = normalized.substring(0, firstSpace);
		String lastName = normalized.substring(firstSpace + 1).trim();
		return new String[]{firstName, isBlank(lastName) ? null : lastName};
	}
}

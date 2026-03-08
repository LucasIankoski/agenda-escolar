package com.escolar.agenda.service;

import com.escolar.agenda.dto.student.StudentCreateRequest;
import com.escolar.agenda.dto.student.StudentResponse;
import com.escolar.agenda.entity.Classroom;
import com.escolar.agenda.entity.Student;
import com.escolar.agenda.entity.UserApp;
import com.escolar.agenda.enums.UserType;
import com.escolar.agenda.repository.ClassroomRepository;
import com.escolar.agenda.repository.StudentRepository;
import com.escolar.agenda.repository.UserRepository;
import com.escolar.agenda.util.LoginNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StudentService {

	private final StudentRepository studentRepository;
	private final ClassroomRepository classroomRepository;

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Transactional
	public StudentResponse create(StudentCreateRequest req) {
		Classroom classroom = classroomRepository.findById(req.classroomId())
				.orElseThrow(() -> new NoSuchElementException("Turma nao encontrada"));

		String parentContactLogin = LoginNormalizer.normalize(req.parentContact());
		if (!LoginNormalizer.isBrazilianMobilePhone(parentContactLogin)) {
			throw new IllegalArgumentException("Contato do responsavel deve ser um celular valido");
		}

		if (userRepository.existsByEmail(parentContactLogin)) {
			throw new IllegalArgumentException("Ja existe usuario com esse contato/login");
		}

		UserApp parent = new UserApp();
		parent.setName(buildFullName(req.parentName(), req.parentLastName()));
		parent.setEmail(parentContactLogin);
		parent.setPassword(passwordEncoder.encode("12345"));
		parent.setType(UserType.PAI);
		parent.setActive(true);
		parent = userRepository.save(parent);

		Student student = new Student();
		student.setName(req.name());
		student.setLastName(req.lastName());
		student.setResponsibleName(req.parentName().trim());
		student.setResponsibleLastName(req.parentLastName().trim());
		student.setResponsibleContact(parentContactLogin);
		student.setBirthDate(req.birthDate());
		student.setClassroom(classroom);
		student.setParentUser(parent);

		student = studentRepository.save(student);
		return toResponse(student);
	}

	public List<StudentResponse> list(UserApp loggedUser) {
		if (isParent(loggedUser)) {
			return studentRepository.findAllByParentUserId(loggedUser.getId())
					.stream()
					.map(this::toResponse)
					.toList();
		}

		validateCanViewAllStudents(loggedUser);
		return studentRepository.findAll().stream().map(this::toResponse).toList();
	}

	public List<StudentResponse> listByClassroom(UUID classroomId) {
		classroomRepository.findById(classroomId)
				.orElseThrow(() -> new NoSuchElementException("Turma nao encontrada"));

		return studentRepository.findAllByClassroomId(classroomId).stream()
				.map(this::toResponse)
				.toList();
	}

	public StudentResponse get(UUID id, UserApp loggedUser) {
		Student s = studentRepository.findById(id)
				.orElseThrow(() -> new NoSuchElementException("Aluno nao encontrado"));
		validateStudentAccess(s, loggedUser);
		return toResponse(s);
	}

	public StudentResponse getByResponsible(String responsibleLogin, UserApp loggedUser) {
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

		Student s = studentRepository.findByResponsibleContact(normalizedLogin)
				.or(() -> studentRepository.findByParentUserEmail(normalizedLogin))
				.orElseThrow(() -> new NoSuchElementException("Aluno nao encontrado para o responsavel informado"));
		return toResponse(s);
	}

	public void delete(UUID id) {
		Student s = getEntityOrThrow(id);
		studentRepository.delete(s);
	}

	private StudentResponse toResponse(Student s) {
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
				parentEmail
		);
	}

	private Student getEntityOrThrow(UUID id) {
		return studentRepository.findById(id)
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

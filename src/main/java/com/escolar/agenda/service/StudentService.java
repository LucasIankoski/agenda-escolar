package com.escolar.agenda.service;

import com.escolar.agenda.dto.student.StudentCreateRequest;
import com.escolar.agenda.dto.student.StudentResponse;
import com.escolar.agenda.entity.Classroom;
import com.escolar.agenda.entity.Student;
import com.escolar.agenda.enums.UserType;
import com.escolar.agenda.entity.UserApp;
import com.escolar.agenda.repository.ClassroomRepository;
import com.escolar.agenda.repository.StudentRepository;
import com.escolar.agenda.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
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
				.orElseThrow(() -> new NoSuchElementException("Turma não encontrada"));

		String baseMail = buildEmailBase(req.name(), req.lastName()); // nome.sobrenome
		String email = baseMail; // se quiser, pode adicionar domínio depois (ex: @escola.com)

		if (userRepository.existsByEmail(email)) {
			throw new IllegalArgumentException("Já existe usuário com email: " + email);
		}

		// 1) cria usuário PAI
		UserApp parent = new UserApp();
		parent.setName(req.name() + " " + req.lastName()); // se sua entidade tiver campo name/nome
		parent.setEmail(email);
		parent.setPassword(passwordEncoder.encode("12345"));
		parent.setType(UserType.PAI); // ou setRole / setType etc conforme sua entidade
		parent.setActive(true);          // se tiver ativo
		parent = userRepository.save(parent);

		// 2) cria Student e vincula parentUser
		Student student = new Student();
		student.setName(req.name());
		student.setLastName(req.lastName());
		student.setBirthDate(req.birthDate());
		student.setClassroom(classroom);
		student.setParentUser(parent);

		student = studentRepository.save(student);

		return toResponse(student);
	}

	public List<StudentResponse> list() {
		return studentRepository.findAll().stream().map(this::toResponse).toList();
	}

	public List<StudentResponse> listByClassroom(UUID classroomId) {
		classroomRepository.findById(classroomId)
				.orElseThrow(() -> new NoSuchElementException("Turma nÃ£o encontrada"));

		return studentRepository.findAllByClassroomId(classroomId).stream()
				.map(this::toResponse)
				.toList();
	}

	public StudentResponse get(UUID id) {
		Student s = studentRepository.findById(id)
				.orElseThrow(() -> new NoSuchElementException("Aluno não encontrado"));
		return toResponse(s);
	}

	public void delete(UUID id) {
		Student s = getEntityOrThrow(id);
		studentRepository.delete(s);
	}

	private StudentResponse toResponse(Student s) {
		UUID parentId = s.getParentUser() != null ? s.getParentUser().getId() : null;
		String parentEmail = s.getParentUser() != null ? s.getParentUser().getEmail() : null;

		return new StudentResponse(
				s.getId(),
				s.getName(),
				s.getLastName(),
				s.getBirthDate(),
				s.getClassroom().getId(),
				parentId,
				parentEmail
		);
	}

	private Student getEntityOrThrow(UUID id) {
		return studentRepository.findById(id)
				.orElseThrow(() -> new NoSuchElementException("Aluno não encontrado"));
	}

	/**
	 * Gera "nome.sobrenome" normalizado:
	 * - minúsculo
	 * - sem acentos
	 * - espaços removidos
	 */
	private String buildEmailBase(String name, String lastName) {
		String full = (name + "." + lastName).toLowerCase();
		full = Normalizer.normalize(full, Normalizer.Form.NFD)
				.replaceAll("\\p{M}", ""); // remove acentos
		full = full.replaceAll("[^a-z0-9.]", ""); // remove tudo que não seja a-z 0-9 ou .
		full = full.replaceAll("\\.+", "."); // evita pontos repetidos
		full = full.replaceAll("^\\.|\\.$", ""); // remove ponto no começo/fim
		return full;
	}
}


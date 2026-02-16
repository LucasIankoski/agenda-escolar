package com.escolar.agenda.service;

import com.escolar.agenda.dto.auth.UserResponse;
import com.escolar.agenda.entity.UserApp;
import com.escolar.agenda.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public UserApp getByEmailOrThrow(String email) {
		return userRepository.findByEmail(email)
				.orElseThrow(() -> new NoSuchElementException("Usuário não encontrado"));
	}

	public UserApp getByIdOrThrow(UUID id) {
		return userRepository.findById(id)
				.orElseThrow(() -> new NoSuchElementException("Usuário não encontrado"));
	}

	public List<UserResponse> listar() {
		return userRepository.findAll().stream().map(this::toResponse).toList();
	}

	public UserResponse buscar(UUID id) {
		return toResponse(getByIdOrThrow(id));
	}

	public UserResponse desativar(UUID id) {
		UserApp u = getByIdOrThrow(id);
		u.setActive(false);
		return toResponse(userRepository.save(u));
	}

	// usado pelo AuthService (registro)
	public UserApp criarUsuario(UserApp usuarioComSenhaEmClaro) {
		usuarioComSenhaEmClaro.setPassword(passwordEncoder.encode(usuarioComSenhaEmClaro.getPassword()));
		return userRepository.save(usuarioComSenhaEmClaro);
	}

	public UserResponse toResponse(UserApp u) {
		return new UserResponse(u.getId(), u.getName(), u.getEmail(), u.getType(), u.isEnabled());
	}
}

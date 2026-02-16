package com.escolar.agenda.service;

import com.escolar.agenda.dto.auth.*;
import com.escolar.agenda.entity.UserApp;
import com.escolar.agenda.repository.UserRepository;
import com.escolar.agenda.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository userRepository;
	private final UserService userService;
	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;

	public AuthResponse register(RegisterRequest request) {
		if (userRepository.existsByEmail(request.email())) {
			throw new IllegalArgumentException("Email já cadastrado");
		}

		UserApp novo = UserApp.builder()
				.name(request.nome())
				.email(request.email())
				.password(request.password()) // será criptografada no UsuarioService
				.type(request.type())
				.active(true)
				.build();

		UserApp salvo = userService.criarUsuario(novo);
		String token = jwtService.generateToken(salvo);

		return new AuthResponse(token, jwtService.getExpirationMs());
	}

	public AuthResponse login(LoginRequest request) {
		Authentication auth = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(request.email(), request.password())
		);

		UserApp usuario = (UserApp) auth.getPrincipal();
		String token = jwtService.generateToken(usuario);

		return new AuthResponse(token, jwtService.getExpirationMs());
	}
}

package com.escolar.agenda.security;

import com.escolar.agenda.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserAppDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		TenantUsername tenantUsername;
		try {
			tenantUsername = TenantUsername.parse(username);
		} catch (IllegalArgumentException e) {
			throw new UsernameNotFoundException("Usuario nao encontrado", e);
		}

		if (TenantUsername.PLATFORM_SLUG.equals(tenantUsername.schoolSlug())) {
			return userRepository.findBySchoolIsNullAndEmail(tenantUsername.login())
					.orElseThrow(() -> new UsernameNotFoundException("Usuario nao encontrado"));
		}

		return userRepository.findBySchoolSlugAndEmail(tenantUsername.schoolSlug(), tenantUsername.login())
				.orElseThrow(() -> new UsernameNotFoundException("Usuario nao encontrado"));
	}

	public UserDetails loadUserByIdAndSchoolId(UUID userId, UUID schoolId) throws UsernameNotFoundException {
		if (schoolId == null) {
			return userRepository.findByIdAndSchoolIsNull(userId)
					.orElseThrow(() -> new UsernameNotFoundException("Usuario nao encontrado"));
		}
		return userRepository.findByIdAndSchoolId(userId, schoolId)
				.orElseThrow(() -> new UsernameNotFoundException("Usuario nao encontrado"));
	}
}

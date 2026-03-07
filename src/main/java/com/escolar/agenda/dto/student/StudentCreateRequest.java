package com.escolar.agenda.dto.student;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.sql.Timestamp;
import java.util.UUID;

public record StudentCreateRequest(
		@NotBlank String name,
		@NotBlank String lastName,
		Timestamp birthDate,
		@NotNull UUID classroomId,
		@NotBlank @JsonAlias("nomeResponsavel") String parentName,
		@NotBlank @JsonAlias("sobrenomeResponsavel") String parentLastName,
		@NotBlank @JsonAlias({"contatoResponsavel", "contato"}) String parentContact
) {}

package com.escolar.agenda.dto.student;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ParentNoteCreateRequest(
		@NotBlank(message = "Informe o recado.")
		@Size(max = 1000, message = "O recado deve ter no máximo 1000 caracteres.")
		@JsonAlias({"content", "recado"})
		String message
) {}

package com.escolar.agenda.dto.platform;

import com.escolar.agenda.dto.auth.UserResponse;
import com.escolar.agenda.dto.school.SchoolResponse;

public record PlatformSchoolCreatedResponse(
		SchoolResponse school,
		UserResponse admin
) {
}

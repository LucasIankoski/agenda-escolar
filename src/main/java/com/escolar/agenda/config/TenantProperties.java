package com.escolar.agenda.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.tenant")
public class TenantProperties {

	private String defaultSchoolName = "Escola Padrao";
	private String defaultSchoolSlug = "default";

	public String getDefaultSchoolName() {
		return defaultSchoolName;
	}

	public void setDefaultSchoolName(String defaultSchoolName) {
		this.defaultSchoolName = defaultSchoolName;
	}

	public String getDefaultSchoolSlug() {
		return defaultSchoolSlug;
	}

	public void setDefaultSchoolSlug(String defaultSchoolSlug) {
		this.defaultSchoolSlug = defaultSchoolSlug;
	}
}

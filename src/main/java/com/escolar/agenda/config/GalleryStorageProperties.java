package com.escolar.agenda.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;
import java.nio.file.Paths;

@ConfigurationProperties(prefix = "app.gallery.storage")
public class GalleryStorageProperties {

	private String rootPath = "storage/student-gallery";

	public String getRootPath() {
		return rootPath;
	}

	public void setRootPath(String rootPath) {
		this.rootPath = rootPath;
	}

	public Path resolveRootPath() {
		return Paths.get(rootPath).toAbsolutePath().normalize();
	}
}

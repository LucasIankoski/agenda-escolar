package com.escolar.agenda.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "student_gallery_photo")
public class StudentGalleryPhoto {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "school_id", nullable = false,
			foreignKey = @ForeignKey(name = "fk_student_gallery_photo_school"))
	private School school;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "student_id", nullable = false,
			foreignKey = @ForeignKey(name = "fk_student_gallery_photo_student"))
	private Student student;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "created_by", nullable = false,
			foreignKey = @ForeignKey(name = "fk_student_gallery_photo_created_by"))
	private UserApp createdBy;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "caption", length = 240)
	private String caption;

	@Column(name = "original_file_name", nullable = false, length = 255)
	private String originalFileName;

	@Column(name = "storage_path", nullable = false, length = 600)
	private String storagePath;

	@Column(name = "thumbnail_path", nullable = false, length = 600)
	private String thumbnailPath;

	@Column(name = "content_type", nullable = false, length = 120)
	private String contentType;

	@Column(name = "size_in_bytes", nullable = false)
	private Long sizeInBytes;

	@Column(name = "width", nullable = false)
	private Integer width;

	@Column(name = "height", nullable = false)
	private Integer height;
}

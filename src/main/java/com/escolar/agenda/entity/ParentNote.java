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
@Table(name = "parent_note")
public class ParentNote {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "school_id", nullable = false,
			foreignKey = @ForeignKey(name = "fk_parent_note_school"))
	private School school;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "student_id", nullable = false,
			foreignKey = @ForeignKey(name = "fk_parent_note_student"))
	private Student student;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "created_by", nullable = false,
			foreignKey = @ForeignKey(name = "fk_parent_note_created_by"))
	private UserApp createdBy;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "message", nullable = false, columnDefinition = "TEXT")
	private String message;

	@Column(name = "read", nullable = false)
	private boolean read = false;

	@Column(name = "read_at")
	private LocalDateTime readAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "read_by",
			foreignKey = @ForeignKey(name = "fk_parent_note_read_by"))
	private UserApp readBy;
}

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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "student")
public class Student {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(nullable = false, length = 120)
	private String name;

	@Column(name = "last_name", nullable = false, length = 120)
	private String lastName;

	@Column(name = "birth_date")
	private Timestamp birthDate;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "classroom_id", nullable = false,
			foreignKey = @ForeignKey(name = "fk_student_classroom"))
	private Classroom classroom;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_user_id",
			foreignKey = @ForeignKey(name = "fk_student_parent_user"))
	private UserApp parentUser;
}

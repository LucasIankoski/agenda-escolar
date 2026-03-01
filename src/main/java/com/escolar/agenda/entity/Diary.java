package com.escolar.agenda.entity;

import com.escolar.agenda.enums.DiaryVersion;
import com.escolar.agenda.enums.FoodLevel;
import com.escolar.agenda.enums.HygieneType;
import com.escolar.agenda.enums.Mood;
import com.escolar.agenda.enums.StoolAspect;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "diary")
public class Diary {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "student_id", nullable = false,
			foreignKey = @ForeignKey(name = "fk_diary_student"))
	private Student student;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "created_by", nullable = false,
			foreignKey = @ForeignKey(name = "fk_diary_created_by"))
	private UserApp createdBy;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Enumerated(EnumType.STRING)
	@Column(name = "diary_version", nullable = false)
	private DiaryVersion diaryVersion = DiaryVersion.V1;

	@Column(name = "v2_payload", columnDefinition = "TEXT")
	private String v2Payload;

	@Enumerated(EnumType.STRING)
	@Column(name = "food_level", nullable = false)
	private FoodLevel foodLevel;

	@Column(name = "food_notes")
	private String foodNotes;

	@Column(name = "sleep_start")
	private LocalDateTime sleepStart;

	@Column(name = "sleep_end")
	private LocalDateTime sleepEnd;

	@Enumerated(EnumType.STRING)
	@Column(name = "hygiene_type")
	private HygieneType hygieneType;

	@Column(name = "hygiene_count")
	private Integer hygieneCount;

	@Column(name = "pee")
	private Boolean pee;

	@Column(name = "poop")
	private Boolean poop;

	@Enumerated(EnumType.STRING)
	@Column(name = "stool_aspect")
	private StoolAspect stoolAspect;

	@Column(name = "hygiene_notes")
	private String hygieneNotes;

	@Enumerated(EnumType.STRING)
	@Column(name = "mood", nullable = false)
	private Mood mood;

	@Column(name = "activities")
	private String activities;

	@Column(name = "read", nullable = false)
	private boolean read = false;

	@Column(name = "read_at")
	private LocalDateTime readAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "read_by",
			foreignKey = @ForeignKey(name = "fk_diary_read_by"))
	private UserApp readBy;
}

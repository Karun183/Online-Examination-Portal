package com.examportal.model;

import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "tests")
public class Test {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(nullable = false)
	private String title;
	@Column(length = 2000)
	private String description;
	@Column(nullable = false)
	private int durationMinutes;
	@Column(nullable = false)
	private boolean published = true;
	@OneToMany(mappedBy = "test", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("id ASC")
	private List<Question> questions = new ArrayList<>();

	public Test() {
	}

	public Long getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String v) {
		title = v;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String v) {
		description = v;
	}

	public int getDurationMinutes() {
		return durationMinutes;
	}

	public void setDurationMinutes(int v) {
		durationMinutes = v;
	}

	public boolean isPublished() {
		return published;
	}

	public void setPublished(boolean v) {
		published = v;
	}

	public List<Question> getQuestions() {
		return questions;
	}
}

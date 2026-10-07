package com.examportal.model;

import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "attempts", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "test_id" }))
public class Attempt {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "test_id", nullable = false)
	private Test test;
	@Column(nullable = false)
	private Instant startedAt;
	private Instant submittedAt;
	private int correctAnswers;
	private int totalQuestions;
	private double percentage;
	@Column(nullable = false)
	private boolean submitted;

	public Attempt() {
	}

	public Long getId() {
		return id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User v) {
		user = v;
	}

	public Test getTest() {
		return test;
	}

	public void setTest(Test v) {
		test = v;
	}

	public Instant getStartedAt() {
		return startedAt;
	}

	public void setStartedAt(Instant v) {
		startedAt = v;
	}

	public Instant getSubmittedAt() {
		return submittedAt;
	}

	public void setSubmittedAt(Instant v) {
		submittedAt = v;
	}

	public int getCorrectAnswers() {
		return correctAnswers;
	}

	public void setCorrectAnswers(int v) {
		correctAnswers = v;
	}

	public int getTotalQuestions() {
		return totalQuestions;
	}

	public void setTotalQuestions(int v) {
		totalQuestions = v;
	}

	public double getPercentage() {
		return percentage;
	}

	public void setPercentage(double v) {
		percentage = v;
	}

	public boolean isSubmitted() {
		return submitted;
	}

	public void setSubmitted(boolean v) {
		submitted = v;
	}
}

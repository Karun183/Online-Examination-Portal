package com.examportal.model;

import jakarta.persistence.*;

@Entity
@Table(name = "answers")
public class Answer {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "attempt_id", nullable = false)
	private Attempt attempt;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "question_id", nullable = false)
	private Question question;
	private String selectedOption;

	public Answer() {
	}

	public Long getId() {
		return id;
	}

	public Attempt getAttempt() {
		return attempt;
	}

	public void setAttempt(Attempt v) {
		attempt = v;
	}

	public Question getQuestion() {
		return question;
	}

	public void setQuestion(Question v) {
		question = v;
	}

	public String getSelectedOption() {
		return selectedOption;
	}

	public void setSelectedOption(String v) {
		selectedOption = v;
	}
}

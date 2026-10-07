package com.examportal.model;

import jakarta.persistence.*;

@Entity
@Table(name = "questions")
public class Question {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "test_id", nullable = false)
	private Test test;
	@Column(nullable = false, length = 3000)
	private String text;
	@Column(nullable = false)
	private String optionA;
	@Column(nullable = false)
	private String optionB;
	@Column(nullable = false)
	private String optionC;
	@Column(nullable = false)
	private String optionD;
	@Column(nullable = false)
	private String correctOption;

	public Question() {
	}

	public Long getId() {
		return id;
	}

	public Test getTest() {
		return test;
	}

	public void setTest(Test v) {
		test = v;
	}

	public String getText() {
		return text;
	}

	public void setText(String v) {
		text = v;
	}

	public String getOptionA() {
		return optionA;
	}

	public void setOptionA(String v) {
		optionA = v;
	}

	public String getOptionB() {
		return optionB;
	}

	public void setOptionB(String v) {
		optionB = v;
	}

	public String getOptionC() {
		return optionC;
	}

	public void setOptionC(String v) {
		optionC = v;
	}

	public String getOptionD() {
		return optionD;
	}

	public void setOptionD(String v) {
		optionD = v;
	}

	public String getCorrectOption() {
		return correctOption;
	}

	public void setCorrectOption(String v) {
		correctOption = v;
	}
}

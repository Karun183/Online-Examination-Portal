package com.examportal.dto;

import jakarta.validation.constraints.*;
import java.time.*;
import java.util.*;

public final class ExamDtos {
	private ExamDtos() {
	}

	public record TestRequest(@NotBlank String title, String description, @Min(1) int durationMinutes,
			boolean published) {
	}

	public record QuestionRequest(@NotBlank String text, @NotBlank String optionA, @NotBlank String optionB,
			@NotBlank String optionC, @NotBlank String optionD, @Pattern(regexp = "[ABCD]") String correctOption) {
	}

	public record TestSummary(Long id, String title, String description, int durationMinutes, boolean published,
			int questionCount) {
	}

	public record QuestionView(Long id, String text, String optionA, String optionB, String optionC, String optionD) {
	}

	public record TestView(Long id, String title, String description, int durationMinutes,
			List<QuestionView> questions) {
	}

	public record StartAttemptResponse(Long attemptId, Instant startedAt, Instant expiresAt, TestView test) {
	}

	public record AnswerInput(Long questionId, @Pattern(regexp = "[ABCD]") String selectedOption) {
	}

	public record SubmitRequest(List<AnswerInput> answers) {
	}

	public record ResultView(Long attemptId, Long testId, String testTitle, String userName, int correctAnswers,
			int totalQuestions, double percentage, Instant startedAt, Instant submittedAt, boolean submitted) {
	}
}
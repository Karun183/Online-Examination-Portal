package com.examportal.service;

import com.examportal.dto.ExamDtos.*;
import com.examportal.model.*;
import com.examportal.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.time.*;
import java.util.*;

@Service
@Transactional
public class ExamService {
	private final TestRepository tests;
	private final AttemptRepository attempts;
	private final AnswerRepository answers;
	private final UserRepository users;

	public ExamService(TestRepository tests, AttemptRepository attempts, AnswerRepository answers,
			UserRepository users) {
		this.tests = tests;
		this.attempts = attempts;
		this.answers = answers;
		this.users = users;
	}

	public List<TestSummary> list(boolean admin) {
		return (admin ? tests.findAll() : tests.findByPublishedTrue()).stream().map(this::summary).toList();
	}

	public TestSummary create(TestRequest r) {
		Test t = new Test();
		t.setTitle(r.title());
		t.setDescription(r.description());
		t.setDurationMinutes(r.durationMinutes());
		t.setPublished(r.published());
		return summary(tests.save(t));
	}

	public TestSummary update(Long id, TestRequest r) {
		Test t = getTest(id);
		t.setTitle(r.title());
		t.setDescription(r.description());
		t.setDurationMinutes(r.durationMinutes());
		t.setPublished(r.published());
		return summary(t);
	}

	public void delete(Long id) {
		tests.delete(getTest(id));
	}

	public void addQuestion(Long testId, QuestionRequest r) {
		Test t = getTest(testId);
		Question q = new Question();
		q.setTest(t);
		q.setText(r.text());
		q.setOptionA(r.optionA());
		q.setOptionB(r.optionB());
		q.setOptionC(r.optionC());
		q.setOptionD(r.optionD());
		q.setCorrectOption(r.correctOption());
		t.getQuestions().add(q);
		tests.save(t);
	}

	public TestView adminTest(Long id) {
		return view(getTest(id), true);
	}

	public StartAttemptResponse start(Long testId, String email) {
		User u = getUser(email);
		Test t = getTest(testId);
		if (!t.isPublished())
			throw bad("This test is not available");
		Attempt a = attempts.findByUserIdAndTestId(u.getId(), testId).orElseGet(() -> {
			Attempt x = new Attempt();
			x.setUser(u);
			x.setTest(t);
			x.setStartedAt(Instant.now());
			x.setTotalQuestions(t.getQuestions().size());
			return attempts.save(x);
		});
		if (a.isSubmitted())
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Test was already submitted");
		Instant end = a.getStartedAt().plusSeconds(t.getDurationMinutes() * 60L);
		if (Instant.now().isAfter(end)) {
			// Persist the automatic submission. The client receives an already-expired
			// attempt and immediately loads the resulting score through submit().
			finish(a, List.of());
		}
		return new StartAttemptResponse(a.getId(), a.getStartedAt(), end, view(t, false));
	}

	public ResultView submit(Long testId, String email, SubmitRequest r) {
		Attempt a = attempts.findByUserIdAndTestId(getUser(email).getId(), testId)
				.orElseThrow(() -> bad("Start the test first"));
		return finish(a, r.answers() == null ? List.of() : r.answers());
	}

	public List<ResultView> myResults(String email) {
		return attempts.findByUserIdOrderByStartedAtDesc(getUser(email).getId()).stream().map(this::result).toList();
	}

	public List<ResultView> allResults() {
		return attempts.findAllDetailed().stream().map(this::result).toList();
	}

	private ResultView finish(Attempt attempt, List<AnswerInput> submittedAnswers) {
		if (attempt.isSubmitted()) {
			return result(attempt);
		}

		Map<Long, String> selectedAnswers = new HashMap<>();
		for (AnswerInput answerInput : submittedAnswers) {
			if (answerInput.questionId() != null && answerInput.selectedOption() != null) {
				selectedAnswers.put(answerInput.questionId(), answerInput.selectedOption());
			}
		}

		int correctAnswers = 0;
		for (Question question : attempt.getTest().getQuestions()) {
			String selectedOption = selectedAnswers.get(question.getId());

			Answer answer = new Answer();
			answer.setAttempt(attempt);
			answer.setQuestion(question);
			answer.setSelectedOption(selectedOption);
			answers.save(answer);

			if (question.getCorrectOption().equals(selectedOption)) {
				correctAnswers++;
			}
		}

		int totalQuestions = attempt.getTest().getQuestions().size();
		attempt.setCorrectAnswers(correctAnswers);
		attempt.setTotalQuestions(totalQuestions);
		attempt.setPercentage(totalQuestions == 0 ? 0.0
				: Math.round((correctAnswers * 10000.0) / totalQuestions) / 100.0);
		attempt.setSubmittedAt(Instant.now());
		attempt.setSubmitted(true);

		return result(attempt);
	}

	private Test getTest(Long id) {
		return tests.findWithQuestionsById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Test not found"));
	}

	private User getUser(String email) {
		return users.findByEmail(email)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
	}

	private ResponseStatusException bad(String m) {
		return new ResponseStatusException(HttpStatus.BAD_REQUEST, m);
	}

	private TestSummary summary(Test t) {
		return new TestSummary(t.getId(), t.getTitle(), t.getDescription(), t.getDurationMinutes(), t.isPublished(),
				t.getQuestions().size());
	}

	private TestView view(Test t, boolean includeCorrect) {
		return new TestView(t.getId(), t.getTitle(), t.getDescription(), t.getDurationMinutes(),
				t.getQuestions().stream().map(q -> new QuestionView(q.getId(), q.getText(), q.getOptionA(),
						q.getOptionB(), q.getOptionC(), q.getOptionD())).toList());
	}

	private ResultView result(Attempt a) {
		return new ResultView(a.getId(), a.getTest().getId(), a.getTest().getTitle(), a.getUser().getName(),
				a.getCorrectAnswers(), a.getTotalQuestions(), a.getPercentage(), a.getStartedAt(), a.getSubmittedAt(),
				a.isSubmitted());
	}
}

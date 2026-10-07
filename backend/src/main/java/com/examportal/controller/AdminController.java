package com.examportal.controller;

import com.examportal.dto.ExamDtos.*;
import com.examportal.service.ExamService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
	private final ExamService service;

	public AdminController(ExamService service) {
		this.service = service;
	}

	@GetMapping("/tests")
	public List<TestSummary> list() {
		return service.list(true);
	}

	@GetMapping("/tests/{id}")
	public TestView one(@PathVariable Long id) {
		return service.adminTest(id);
	}

	@PostMapping("/tests")
	public ResponseEntity<TestSummary> create(@Valid @RequestBody TestRequest r) {
		return ResponseEntity.status(HttpStatus.CREATED).body(service.create(r));
	}

	@PutMapping("/tests/{id}")
	public TestSummary update(@PathVariable Long id, @Valid @RequestBody TestRequest r) {
		return service.update(id, r);
	}

	@DeleteMapping("/tests/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void remove(@PathVariable Long id) {
		service.delete(id);
	}

	@PostMapping("/tests/{id}/questions")
	@ResponseStatus(HttpStatus.CREATED)
	public void question(@PathVariable Long id, @Valid @RequestBody QuestionRequest r) {
		service.addQuestion(id, r);
	}

	@GetMapping("/results")
	public List<ResultView> results() {
		return service.allResults();
	}
}

package com.examportal.controller;

import com.examportal.dto.ExamDtos.*;
import com.examportal.service.ExamService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api")
public class TestController {
	private final ExamService service;

	public TestController(ExamService service) {
		this.service = service;
	}

	@GetMapping("/tests")
	public List<TestSummary> tests() {
		return service.list(false);
	}

	@PostMapping("/tests/{id}/start")
	public StartAttemptResponse start(@PathVariable Long id, Authentication a) {
		return service.start(id, a.getName());
	}

	@PostMapping("/tests/{id}/submit")
	public ResultView submit(@PathVariable Long id, @Valid @RequestBody SubmitRequest r, Authentication a) {
		return service.submit(id, a.getName(), r);
	}

	@GetMapping("/results/me")
	public List<ResultView> results(Authentication a) {
		return service.myResults(a.getName());
	}
}

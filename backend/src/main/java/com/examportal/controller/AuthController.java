package com.examportal.controller;

import com.examportal.dto.AuthDtos.*;
import com.examportal.model.*;
import com.examportal.repository.UserRepository;
import com.examportal.security.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
	private final UserRepository users;
	private final PasswordEncoder encoder;
	private final JwtService jwt;

	public AuthController(UserRepository users, PasswordEncoder encoder, JwtService jwt) {
		this.users = users;
		this.encoder = encoder;
		this.jwt = jwt;
	}

	@PostMapping("/signup")
	public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest r) {
		if (users.existsByEmail(r.email().toLowerCase()))
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		User u = users.save(new User(r.email().toLowerCase(), r.name(), encoder.encode(r.password()), Role.USER));
		return ResponseEntity.status(HttpStatus.CREATED).body(response(u));
	}

	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest r) {
		return users.findByEmail(r.email().toLowerCase()).filter(u -> encoder.matches(r.password(), u.getPassword()))
				.map(this::response).map(ResponseEntity::ok)
				.orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
	}

	@GetMapping("/me")
	public ProfileResponse me(Authentication authentication) {
		User user = users.findByEmail(authentication.getName())
				.orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(HttpStatus.UNAUTHORIZED));
		return new ProfileResponse(user.getName(), user.getEmail(), user.getRole());
	}

	private AuthResponse response(User u) {
		return new AuthResponse(jwt.generate(u.getEmail(), u.getRole().name()), u.getName(), u.getEmail(), u.getRole());
	}
}

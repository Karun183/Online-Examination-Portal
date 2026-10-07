package com.examportal.security;

import com.examportal.repository.UserRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.*;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	private final JwtService jwt;
	private final UserRepository users;

	public JwtAuthenticationFilter(JwtService jwt, UserRepository users) {
		this.jwt = jwt;
		this.users = users;
	}		

	@Override
	protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
			throws ServletException, IOException {
		String h = req.getHeader("Authorization");
		if (h != null && h.startsWith("Bearer ")) {
			String token = h.substring(7);
			if (jwt.valid(token) && SecurityContextHolder.getContext().getAuthentication() == null)
				users.findByEmail(jwt.email(token))
						.ifPresent(u -> SecurityContextHolder.getContext()
								.setAuthentication(new UsernamePasswordAuthenticationToken(u.getEmail(), null,
										List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole())))));
		}
		chain.doFilter(req, res);
	}
}

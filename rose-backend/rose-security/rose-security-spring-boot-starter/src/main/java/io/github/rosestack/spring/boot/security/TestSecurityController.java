package io.github.rosestack.spring.boot.security;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TestSecurityController {

	@GetMapping("/me")
	public String me(Authentication authentication) {
		return authentication != null ? authentication.getName() : "anonymous";
	}
}

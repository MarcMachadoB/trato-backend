package com.trato.backend.auth;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuthController {

    @GetMapping("/health")
    public HealthResponse health() {
        return new HealthResponse("ok");
    }

    @GetMapping("/auth/me")
    public AuthenticatedUser currentUser(
            @RequestAttribute(FirebaseAuthFilter.USER_ATTRIBUTE) AuthenticatedUser user) {
        return user;
    }

    public record HealthResponse(String status) {
    }
}

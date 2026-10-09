package com.trato.backend.auth;

import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

@Component
public class FirebaseAuthFilter extends OncePerRequestFilter {

    static final String USER_ATTRIBUTE = "authenticatedFirebaseUser";
    private static final Logger LOGGER = LoggerFactory.getLogger(FirebaseAuthFilter.class);

    private final FirebaseTokenVerifier tokenVerifier;
    private final ObjectMapper objectMapper;

    public FirebaseAuthFilter(FirebaseTokenVerifier tokenVerifier, ObjectMapper objectMapper) {
        this.tokenVerifier = tokenVerifier;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return "OPTIONS".equalsIgnoreCase(request.getMethod())
                || !path.startsWith("/api/")
                || "/api/health".equals(path);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith("Bearer ")
                || authorization.substring("Bearer ".length()).isBlank()) {
            writeError(response, HttpStatus.UNAUTHORIZED, "Se requiere un Firebase ID token Bearer.");
            return;
        }

        try {
            FirebaseToken token = tokenVerifier.verify(authorization.substring("Bearer ".length()).trim());
            request.setAttribute(USER_ATTRIBUTE, new AuthenticatedUser(
                    token.getUid(),
                    token.getEmail(),
                    token.isEmailVerified()));
        } catch (FirebaseAuthException exception) {
            LOGGER.debug("Rejected invalid Firebase ID token: {}", exception.getMessage());
            writeError(response, HttpStatus.UNAUTHORIZED, "El Firebase ID token no es válido o ha caducado.");
            return;
        } catch (FirebaseAdminUnavailableException exception) {
            LOGGER.error("Firebase Admin token verification is unavailable.", exception);
            writeError(response, HttpStatus.SERVICE_UNAVAILABLE, exception.getMessage());
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void writeError(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), new ApiError(message));
    }
}

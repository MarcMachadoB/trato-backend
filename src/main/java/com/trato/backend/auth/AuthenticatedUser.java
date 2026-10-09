package com.trato.backend.auth;

public record AuthenticatedUser(String uid, String email, boolean emailVerified) {
}

package io.truthchain.model;

public record User(long id, String name, String email, String role, String status, java.time.Instant createdAt) { }

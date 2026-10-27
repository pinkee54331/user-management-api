package com.example.usermanagement.dto;

import com.example.usermanagement.model.Role;
import com.example.usermanagement.model.User;

import java.time.Instant;

public record UserResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phone,
        Role role,
        Instant createdAt,
        Instant updatedAt
) {
    public static UserResponse from(User u) {
        return new UserResponse(u.getId(), u.getFirstName(), u.getLastName(),
                u.getEmail(), u.getPhone(), u.getRole(), u.getCreatedAt(), u.getUpdatedAt());
    }
}

package com.example.usermanagement.dto;

import com.example.usermanagement.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UserRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank @Email String email,
        @Pattern(regexp = "^\\+?[0-9\\- ]{7,20}$", message = "must be a valid phone number") String phone,
        Role role
) {
}

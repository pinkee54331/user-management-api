package com.example.usermanagement.controller;

import com.example.usermanagement.dto.UserRequest;
import com.example.usermanagement.dto.UserResponse;
import com.example.usermanagement.service.UserService;
import com.example.usermanagement.config.ApiScopes;
import com.example.usermanagement.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @Operation(summary = "List users", description = "Requires scope `users:read`.",
            security = @SecurityRequirement(name = OpenApiConfig.GOOGLE_LOGIN, scopes = ApiScopes.USERS_READ))
    @GetMapping
    public Page<UserResponse> list(Pageable pageable) {
        return service.list(pageable);
    }

    @Operation(summary = "Get a user", description = "Requires scope `users:read`.",
            security = @SecurityRequirement(name = OpenApiConfig.GOOGLE_LOGIN, scopes = ApiScopes.USERS_READ))
    @GetMapping("/{id}")
    public UserResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @Operation(summary = "Create a user", description = "Requires scope `users:write`.",
            security = @SecurityRequirement(name = OpenApiConfig.GOOGLE_LOGIN, scopes = ApiScopes.USERS_WRITE))
    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserRequest request) {
        UserResponse created = service.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @Operation(summary = "Update a user", description = "Requires scope `users:write`.",
            security = @SecurityRequirement(name = OpenApiConfig.GOOGLE_LOGIN, scopes = ApiScopes.USERS_WRITE))
    @PutMapping("/{id}")
    public UserResponse update(@PathVariable Long id, @Valid @RequestBody UserRequest request) {
        return service.update(id, request);
    }

    @Operation(summary = "Delete a user", description = "Requires scope `users:delete`.",
            security = @SecurityRequirement(name = OpenApiConfig.GOOGLE_LOGIN, scopes = ApiScopes.USERS_DELETE))
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

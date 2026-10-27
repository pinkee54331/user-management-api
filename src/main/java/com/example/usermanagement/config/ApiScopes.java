package com.example.usermanagement.config;

import com.example.usermanagement.model.Role;

import java.util.Map;
import java.util.Set;

/** Scopes an API call can require, and which scopes each role is granted at login. */
public final class ApiScopes {

    public static final String USERS_READ = "users:read";
    public static final String USERS_WRITE = "users:write";
    public static final String USERS_DELETE = "users:delete";

    private static final Map<Role, Set<String>> SCOPES_BY_ROLE = Map.of(
            Role.ADMIN, Set.of(USERS_READ, USERS_WRITE, USERS_DELETE),
            Role.MANAGER, Set.of(USERS_READ, USERS_WRITE),
            Role.USER, Set.of(USERS_READ));

    private ApiScopes() {
    }

    public static Set<String> forRole(Role role) {
        return SCOPES_BY_ROLE.get(role);
    }
}

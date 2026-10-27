package com.example.usermanagement.config;

import com.example.usermanagement.model.Role;
import com.example.usermanagement.model.User;
import com.example.usermanagement.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner loadSampleUsers(UserRepository repository) {
        return args -> {
            List<User> samples = List.of(
                    user("John", "Doe", "john.doe@example.com", "555-0101", Role.ADMIN),
                    user("Jane", "Smith", "jane.smith@example.com", "555-0102", Role.MANAGER),
                    user("Alice", "Johnson", "alice.johnson@example.com", "555-0103", Role.USER),
                    user("Bob", "Williams", "bob.williams@example.com", "555-0104", Role.USER),
                    user("Carol", "Brown", "carol.brown@example.com", null, Role.MANAGER));

            // The H2 database is file-based, so add missing users and only fill in the
            // role of users stored before the role field existed.
            for (User sample : samples) {
                repository.findByEmailIgnoreCase(sample.getEmail()).ifPresentOrElse(
                        existing -> {
                            if (existing.getRole() == Role.USER && sample.getRole() != Role.USER) {
                                existing.setRole(sample.getRole());
                                repository.save(existing);
                            }
                        },
                        () -> repository.save(sample));
            }
        };
    }

    private static User user(String firstName, String lastName, String email, String phone, Role role) {
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setRole(role);
        return user;
    }
}

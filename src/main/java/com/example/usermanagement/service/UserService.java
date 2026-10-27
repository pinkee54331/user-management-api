package com.example.usermanagement.service;

import com.example.usermanagement.dto.UserRequest;
import com.example.usermanagement.dto.UserResponse;
import com.example.usermanagement.exception.EmailAlreadyExistsException;
import com.example.usermanagement.exception.UserNotFoundException;
import com.example.usermanagement.model.User;
import com.example.usermanagement.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> list(Pageable pageable) {
        return repository.findAll(pageable).map(UserResponse::from);
    }

    @Transactional(readOnly = true)
    public UserResponse get(Long id) {
        return UserResponse.from(find(id));
    }

    public UserResponse create(UserRequest req) {
        if (repository.existsByEmailIgnoreCase(req.email())) {
            throw new EmailAlreadyExistsException(req.email());
        }
        User user = new User();
        apply(user, req);
        return UserResponse.from(repository.save(user));
    }

    public UserResponse update(Long id, UserRequest req) {
        User user = find(id);
        if (repository.existsByEmailIgnoreCaseAndIdNot(req.email(), id)) {
            throw new EmailAlreadyExistsException(req.email());
        }
        apply(user, req);
        return UserResponse.from(user);
    }

    public void delete(Long id) {
        repository.delete(find(id));
    }

    private User find(Long id) {
        return repository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    private void apply(User user, UserRequest req) {
        user.setFirstName(req.firstName());
        user.setLastName(req.lastName());
        user.setEmail(req.email());
        user.setPhone(req.phone());
        // role is optional in the request; keep the current role (USER for new users) when omitted
        if (req.role() != null) {
            user.setRole(req.role());
        }
    }
}

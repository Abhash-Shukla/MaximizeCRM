package com.maximizecrm.controller;

import com.maximizecrm.dto.UserSummaryResponse;
import com.maximizecrm.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<UserSummaryResponse>> getAssignableUsers() {
        List<UserSummaryResponse> users = userRepository.findAll().stream()
            .filter(u -> u.isActive())
            .map(UserSummaryResponse::fromEntity)
            .toList();
        return ResponseEntity.ok(users);
    }
}


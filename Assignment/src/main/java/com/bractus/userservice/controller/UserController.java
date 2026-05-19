package com.bractus.userservice.controller;

import com.bractus.userservice.dto.LoginRequest;
import com.bractus.userservice.dto.SignupRequest;
import com.bractus.userservice.dto.UserResponse;
import com.bractus.userservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * UserController exposes the REST API for user management.
 * All endpoints are prefixed with /api to avoid conflicts with the Thymeleaf UI routes.
 *
 * Endpoints:
 *   POST /api/signup        - Register a new user
 *   POST /api/login         - Authenticate an existing user
 *   GET  /api/users/{id}    - Get user info by ID
 */
@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Register a new user via REST API.
     * On success, also triggers welcome note creation in Notes Service.
     *
     * @param request body with username and password
     * @return 201 Created with user id and username
     */
    @PostMapping("/signup")
    public ResponseEntity<UserResponse> signup(@Valid @RequestBody SignupRequest request) {
        UserResponse response = userService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Log in an existing user via REST API.
     *
     * @param request body with username and password
     * @return 200 OK with user id and username
     */
    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@Valid @RequestBody LoginRequest request) {
        UserResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get a user's public info by their ID.
     *
     * @param id path variable — the user's MongoDB ID
     * @return 200 OK with user id and username
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable String id) {
        UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(response);
    }
}

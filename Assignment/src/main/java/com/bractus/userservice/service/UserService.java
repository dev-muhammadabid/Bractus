package com.bractus.userservice.service;

import com.bractus.userservice.dto.LoginRequest;
import com.bractus.userservice.dto.NoteCreateRequest;
import com.bractus.userservice.dto.SignupRequest;
import com.bractus.userservice.dto.UserResponse;
import com.bractus.userservice.exception.InvalidCredentialsException;
import com.bractus.userservice.exception.UserAlreadyExistsException;
import com.bractus.userservice.exception.UserNotFoundException;
import com.bractus.userservice.model.User;
import com.bractus.userservice.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * UserService contains all business logic for user management.
 *
 * Responsibilities:
 *  - Signup: validate uniqueness, save user, call Notes Service to create welcome note
 *  - Login: verify credentials
 *  - Lookup: find user by ID
 */
@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final RestTemplate restTemplate;
    private final String notesServiceBaseUrl;

    // Constructor injection — all dependencies are explicit and testable
    public UserService(UserRepository userRepository,
                       RestTemplate restTemplate,
                       String notesServiceBaseUrl) {
        this.userRepository = userRepository;
        this.restTemplate = restTemplate;
        this.notesServiceBaseUrl = notesServiceBaseUrl;
    }

    /**
     * Registers a new user.
     *
     * Steps:
     *  1. Check if username is already taken (case-insensitive)
     *  2. Save the new user to MongoDB
     *  3. Call Notes Service to create a welcome note for the new user
     *
     * @param request DTO with username and password
     * @return UserResponse with the saved user's id and username
     */
    public UserResponse signup(SignupRequest request) {
        // Step 1: Check for duplicate username
        userRepository.findByUsernameIgnoreCase(request.getUsername())
                .ifPresent(existing -> {
                    throw new UserAlreadyExistsException(
                            "Username '" + request.getUsername() + "' is already taken.");
                });

        // Step 2: Build and save the user
        // NOTE: In a production app you would hash the password (e.g. BCrypt).
        // For this beginner-friendly assignment, we store it as plain text.
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());

        User savedUser = userRepository.save(user);
        log.info("New user registered: id={}, username={}", savedUser.getId(), savedUser.getUsername());

        // Step 3: Call Notes Service to create the welcome note
        // This is REST-based inter-service communication — no shared DB, no foreign keys.
        createWelcomeNote(savedUser);

        return new UserResponse(savedUser.getId(), savedUser.getUsername());
    }

    /**
     * Authenticates a user by checking username and password.
     *
     * @param request DTO with username and password
     * @return UserResponse with the user's id and username on success
     */
    public UserResponse login(LoginRequest request) {
        // Find user by username (case-insensitive)
        User user = userRepository.findByUsernameIgnoreCase(request.getUsername())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password."));

        // Simple plain-text password check
        if (!user.getPassword().equals(request.getPassword())) {
            throw new InvalidCredentialsException("Invalid username or password.");
        }

        log.info("User logged in: id={}, username={}", user.getId(), user.getUsername());
        return new UserResponse(user.getId(), user.getUsername());
    }

    /**
     * Retrieves a user by their MongoDB ID.
     *
     * @param id the user's ID
     * @return UserResponse with id and username
     */
    public UserResponse getUserById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        return new UserResponse(user.getId(), user.getUsername());
    }

    /**
     * Calls the Notes Service via REST to create a welcome note for a newly registered user.
     *
     * This is a fire-and-forget style call — if Notes Service is down, we log the error
     * but do NOT fail the signup. The user is already saved at this point.
     *
     * @param user the newly created user
     */
    private void createWelcomeNote(User user) {
        try {
            // Build the welcome note payload
            NoteCreateRequest welcomeNote = new NoteCreateRequest(
                    user.getId(),
                    "Welcome",
                    "Welcome to Note " + user.getUsername()
            );

            // POST to Notes Service: http://localhost:8082/notes
            String notesUrl = notesServiceBaseUrl + "/notes";
            restTemplate.postForObject(notesUrl, welcomeNote, Object.class);

            log.info("Welcome note created for user: {}", user.getUsername());
        } catch (Exception e) {
            // Log the failure but don't break the signup flow
            log.warn("Could not create welcome note for user '{}': {}", user.getUsername(), e.getMessage());
        }
    }
}

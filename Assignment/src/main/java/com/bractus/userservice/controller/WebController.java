package com.bractus.userservice.controller;

import com.bractus.userservice.dto.LoginRequest;
import com.bractus.userservice.dto.NoteCreateRequest;
import com.bractus.userservice.dto.SignupRequest;
import com.bractus.userservice.dto.UserResponse;
import com.bractus.userservice.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * WebController handles all browser-facing routes.
 * It renders Thymeleaf HTML templates and manages the user session.
 *
 * Session attributes used:
 *   - userId   : the logged-in user's MongoDB ID
 *   - username : the logged-in user's display name
 */
@Controller
public class WebController {

    private static final Logger log = LoggerFactory.getLogger(WebController.class);

    private final UserService userService;
    private final RestTemplate restTemplate;
    private final String notesServiceBaseUrl;

    public WebController(UserService userService,
                         RestTemplate restTemplate,
                         String notesServiceBaseUrl) {
        this.userService = userService;
        this.restTemplate = restTemplate;
        this.notesServiceBaseUrl = notesServiceBaseUrl;
    }

    // ─────────────────────────────────────────────
    // Root redirect
    // ─────────────────────────────────────────────

    /** Redirect root URL to login page */
    @GetMapping("/")
    public String root(HttpSession session) {
        if (session.getAttribute("userId") != null) {
            return "redirect:/dashboard";
        }
        return "redirect:/login";
    }

    // ─────────────────────────────────────────────
    // Login
    // ─────────────────────────────────────────────

    /** Show the login form */
    @GetMapping("/login")
    public String loginPage(Model model, HttpSession session) {
        if (session.getAttribute("userId") != null) {
            return "redirect:/dashboard";
        }
        model.addAttribute("loginRequest", new LoginRequest());
        return "login";
    }

    /** Handle login form submission */
    @PostMapping("/login")
    public String loginSubmit(@Valid @ModelAttribute("loginRequest") LoginRequest request,
                              BindingResult bindingResult,
                              HttpSession session,
                              Model model) {
        if (bindingResult.hasErrors()) {
            return "login";
        }

        try {
            UserResponse user = userService.login(request);
            // Store user info in session so we know who is logged in
            session.setAttribute("userId", user.getId());
            session.setAttribute("username", user.getUsername());
            return "redirect:/dashboard";
        } catch (Exception e) {
            model.addAttribute("error", "Invalid username or password.");
            return "login";
        }
    }

    // ─────────────────────────────────────────────
    // Signup
    // ─────────────────────────────────────────────

    /** Show the signup form */
    @GetMapping("/signup")
    public String signupPage(Model model, HttpSession session) {
        if (session.getAttribute("userId") != null) {
            return "redirect:/dashboard";
        }
        model.addAttribute("signupRequest", new SignupRequest());
        return "signup";
    }

    /** Handle signup form submission */
    @PostMapping("/signup")
    public String signupSubmit(@Valid @ModelAttribute("signupRequest") SignupRequest request,
                               BindingResult bindingResult,
                               HttpSession session,
                               Model model) {
        if (bindingResult.hasErrors()) {
            return "signup";
        }

        try {
            UserResponse user = userService.signup(request);
            // Auto-login after successful signup
            session.setAttribute("userId", user.getId());
            session.setAttribute("username", user.getUsername());
            return "redirect:/dashboard";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "signup";
        }
    }

    // ─────────────────────────────────────────────
    // Logout
    // ─────────────────────────────────────────────

    /** Clear session and redirect to login */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    // ─────────────────────────────────────────────
    // Dashboard (Notes)
    // ─────────────────────────────────────────────

    /**
     * Show the notes dashboard.
     * Fetches all notes for the logged-in user from Notes Service.
     */
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        String userId = (String) session.getAttribute("userId");
        String username = (String) session.getAttribute("username");

        // Redirect to login if not authenticated
        if (userId == null) {
            return "redirect:/login";
        }

        // Fetch notes from Notes Service via REST
        List<Map> notes = fetchNotes(userId);

        model.addAttribute("username", username);
        model.addAttribute("userId", userId);
        model.addAttribute("notes", notes);
        model.addAttribute("newNote", new NoteCreateRequest());
        return "dashboard";
    }

    // ─────────────────────────────────────────────
    // Note CRUD (form submissions from dashboard)
    // ─────────────────────────────────────────────

    /** Handle "Create Note" form submission from the dashboard */
    @PostMapping("/notes/create")
    public String createNote(@RequestParam String title,
                             @RequestParam String content,
                             HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        try {
            NoteCreateRequest req = new NoteCreateRequest(userId, title, content);
            restTemplate.postForObject(notesServiceBaseUrl + "/notes", req, Object.class);
        } catch (Exception e) {
            log.warn("Failed to create note: {}", e.getMessage());
        }

        return "redirect:/dashboard";
    }

    /** Handle "Delete Note" button click from the dashboard */
    @PostMapping("/notes/delete/{noteId}")
    public String deleteNote(@PathVariable String noteId, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        try {
            String url = notesServiceBaseUrl + "/notes/" + noteId + "?userId=" + userId;
            restTemplate.delete(url);
        } catch (Exception e) {
            log.warn("Failed to delete note {}: {}", noteId, e.getMessage());
        }

        return "redirect:/dashboard";
    }

    /** Show the edit note form */
    @GetMapping("/notes/edit/{noteId}")
    public String editNotePage(@PathVariable String noteId, HttpSession session, Model model) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        try {
            // Fetch all notes and find the one to edit
            List<Map> notes = fetchNotes(userId);
            Map note = notes.stream()
                    .filter(n -> noteId.equals(n.get("id")))
                    .findFirst()
                    .orElse(null);

            if (note == null) return "redirect:/dashboard";

            model.addAttribute("note", note);
            model.addAttribute("username", session.getAttribute("username"));
            return "edit-note";
        } catch (Exception e) {
            return "redirect:/dashboard";
        }
    }

    /** Handle "Save" on the edit note form */
    @PostMapping("/notes/edit/{noteId}")
    public String editNoteSubmit(@PathVariable String noteId,
                                 @RequestParam String title,
                                 @RequestParam String content,
                                 HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        try {
            String url = notesServiceBaseUrl + "/notes/" + noteId + "?userId=" + userId;
            Map<String, String> body = Map.of("title", title, "content", content);
            restTemplate.put(url, body);
        } catch (Exception e) {
            log.warn("Failed to update note {}: {}", noteId, e.getMessage());
        }

        return "redirect:/dashboard";
    }

    // ─────────────────────────────────────────────
    // Helper
    // ─────────────────────────────────────────────

    /**
     * Fetches notes for a user from the Notes Service.
     * Returns an empty list if the call fails (e.g. Notes Service is down).
     */
    @SuppressWarnings("unchecked")
    private List<Map> fetchNotes(String userId) {
        try {
            ResponseEntity<List<Map>> response = restTemplate.exchange(
                    notesServiceBaseUrl + "/notes/user/" + userId,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Map>>() {}
            );
            return response.getBody() != null ? response.getBody() : List.of();
        } catch (Exception e) {
            log.warn("Could not fetch notes for user {}: {}", userId, e.getMessage());
            return List.of();
        }
    }
}

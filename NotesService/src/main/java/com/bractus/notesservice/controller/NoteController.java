package com.bractus.notesservice.controller;

import com.bractus.notesservice.dto.NoteCreateRequest;
import com.bractus.notesservice.dto.NoteResponse;
import com.bractus.notesservice.dto.NoteUpdateRequest;
import com.bractus.notesservice.model.Note;
import com.bractus.notesservice.service.NoteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * NoteController exposes REST endpoints for the Notes microservice.
 * All routes are prefixed with /notes.
 *
 * Endpoints:
 *   POST   /notes                      - Create a new note
 *   PUT    /notes/{id}?userId=...      - Update a note (ownership verified)
 *   DELETE /notes/{id}?userId=...      - Delete a note (ownership verified)
 *   GET    /notes/user/{userId}        - Get all notes for a user
 *
 * All endpoints return NoteResponse DTOs — never the raw Note entity.
 */
@RestController
@RequestMapping("/notes")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    /**
     * Create a new note.
     * Called by the UI and also by User Service when a new user signs up (welcome note).
     *
     * @param request body with userId, title, content
     * @return 201 Created with the saved note as NoteResponse
     */
    @PostMapping
    public ResponseEntity<NoteResponse> createNote(@Valid @RequestBody NoteCreateRequest request) {
        Note created = noteService.createNote(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(NoteResponse.from(created));
    }

    /**
     * Update an existing note.
     * userId is passed as a query param so the service can verify ownership.
     *
     * @param id      path variable — the note's MongoDB ID
     * @param userId  query param — the requesting user's ID
     * @param request body with new title and content
     * @return 200 OK with the updated note as NoteResponse
     */
    @PutMapping("/{id}")
    public ResponseEntity<NoteResponse> updateNote(
            @PathVariable String id,
            @RequestParam String userId,
            @Valid @RequestBody NoteUpdateRequest request) {

        Note updated = noteService.updateNote(id, userId, request);
        return ResponseEntity.ok(NoteResponse.from(updated));
    }

    /**
     * Delete a note.
     * userId is passed as a query param for ownership verification.
     *
     * @param id     path variable — the note's MongoDB ID
     * @param userId query param — the requesting user's ID
     * @return 204 No Content on success
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNote(
            @PathVariable String id,
            @RequestParam String userId) {

        noteService.deleteNote(id, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get all notes belonging to a specific user.
     * Returns notes sorted newest-first.
     *
     * @param userId path variable — the user's ID
     * @return 200 OK with list of NoteResponse DTOs
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NoteResponse>> getNotesByUser(@PathVariable String userId) {
        List<NoteResponse> notes = noteService.getNotesByUser(userId)
                .stream()
                .map(NoteResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(notes);
    }
}

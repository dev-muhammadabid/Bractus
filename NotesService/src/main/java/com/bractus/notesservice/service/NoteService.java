package com.bractus.notesservice.service;

import com.bractus.notesservice.dto.NoteCreateRequest;
import com.bractus.notesservice.dto.NoteUpdateRequest;
import com.bractus.notesservice.exception.NoteNotFoundException;
import com.bractus.notesservice.exception.NoteOwnershipException;
import com.bractus.notesservice.model.Note;
import com.bractus.notesservice.repository.NoteRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * NoteService handles all business logic for notes.
 * It sits between the controller and the repository layer.
 */
@Service
public class NoteService {

    // Repository for MongoDB operations on notes
    private final NoteRepository noteRepository;

    // Constructor injection — preferred over @Autowired on fields
    public NoteService(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    /**
     * Creates a new note and saves it to MongoDB.
     *
     * @param request DTO containing userId, title, and content
     * @return the saved Note document
     */
    public Note createNote(NoteCreateRequest request) {
        Note note = Note.builder()
                .userId(request.getUserId())
                .title(request.getTitle())
                .content(request.getContent())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        return noteRepository.save(note);
    }

    /**
     * Updates an existing note's title and content.
     * Verifies that the note belongs to the requesting user before updating.
     *
     * @param noteId  the ID of the note to update
     * @param userId  the ID of the user making the request (for ownership check)
     * @param request DTO with new title and content
     * @return the updated Note document
     */
    public Note updateNote(String noteId, String userId, NoteUpdateRequest request) {
        // Find the note or throw 404
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new NoteNotFoundException("Note not found with id: " + noteId));

        // Make sure the note belongs to the requesting user
        if (!note.getUserId().equals(userId)) {
            throw new NoteOwnershipException("You do not have permission to update this note.");
        }

        // Apply updates
        note.setTitle(request.getTitle());
        note.setContent(request.getContent());
        note.setUpdatedAt(Instant.now());

        return noteRepository.save(note);
    }

    /**
     * Deletes a note by ID.
     * Verifies ownership before deleting.
     *
     * @param noteId the ID of the note to delete
     * @param userId the ID of the user making the request
     */
    public void deleteNote(String noteId, String userId) {
        // Find the note or throw 404
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new NoteNotFoundException("Note not found with id: " + noteId));

        // Ownership check
        if (!note.getUserId().equals(userId)) {
            throw new NoteOwnershipException("You do not have permission to delete this note.");
        }

        noteRepository.deleteById(noteId);
    }

    /**
     * Retrieves all notes for a given user, sorted by creation date (newest first).
     *
     * @param userId the user whose notes to fetch
     * @return list of notes
     */
    public List<Note> getNotesByUser(String userId) {
        return noteRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}

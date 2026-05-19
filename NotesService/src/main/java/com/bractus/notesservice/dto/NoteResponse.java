package com.bractus.notesservice.dto;

import com.bractus.notesservice.model.Note;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * NoteResponse is the outbound DTO returned to clients after any note operation.
 *
 * We use a dedicated response DTO instead of returning the raw Note model directly.
 * This keeps the API contract stable even if the internal model changes.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoteResponse {

    private String id;
    private String userId;
    private String title;
    private String content;
    private Instant createdAt;
    private Instant updatedAt;

    /**
     * Convenience factory method — converts a Note entity into a NoteResponse DTO.
     *
     * @param note the saved Note document from MongoDB
     * @return a NoteResponse ready to send to the client
     */
    public static NoteResponse from(Note note) {
        return new NoteResponse(
                note.getId(),
                note.getUserId(),
                note.getTitle(),
                note.getContent(),
                note.getCreatedAt(),
                note.getUpdatedAt()
        );
    }
}

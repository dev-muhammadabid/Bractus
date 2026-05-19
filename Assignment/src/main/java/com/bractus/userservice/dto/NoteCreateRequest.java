package com.bractus.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Outbound DTO sent from User_Service to Notes_Service when creating a note.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoteCreateRequest {

    private String userId;
    private String title;
    private String content;
}

package com.bractus.notesservice.repository;

import com.bractus.notesservice.model.Note;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface NoteRepository extends MongoRepository<Note, String> {

    List<Note> findByUserIdOrderByCreatedAtDesc(String userId);
}

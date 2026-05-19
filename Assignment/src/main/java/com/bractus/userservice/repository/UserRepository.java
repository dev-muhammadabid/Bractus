package com.bractus.userservice.repository;

import com.bractus.userservice.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByUsernameIgnoreCase(String username);
}

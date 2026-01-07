package com.banking_system.repository;

import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.banking_system.model.User;

public interface UserRepository extends MongoRepository<User, ObjectId> {
	Optional<User> findByEmail(String email);

	boolean existsByEmail(String email);
}

package com.financetracker.repository;

import com.financetracker.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByEmail(String email);

    Optional<User> findByGoogleId(String googleId);

    @Query("{ '$and': [ { 'email': { '$regex': ?0, '$options': 'i' } }, { 'email': { '$ne': ?1 } } ] }")
    List<User> searchByEmailFragment(String emailPattern, String excludeEmail);
}

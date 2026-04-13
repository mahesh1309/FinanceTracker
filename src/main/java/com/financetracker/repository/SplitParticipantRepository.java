package com.financetracker.repository;

import com.financetracker.model.SplitParticipant;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SplitParticipantRepository extends MongoRepository<SplitParticipant, String> {
}

package com.financetracker.repository;

import com.financetracker.model.SplitGroup;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SplitGroupRepository extends MongoRepository<SplitGroup, String> {
}

package com.financetracker.repository;

import com.financetracker.model.Split;
import com.financetracker.model.SplitGroup;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SplitRepository extends MongoRepository<Split, String> {

    List<Split> findByGroupOrderByCreatedAtDesc(SplitGroup group);
}

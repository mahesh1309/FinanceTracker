package com.financetracker.repository;

import com.financetracker.model.Expense;
import com.financetracker.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseRepository extends MongoRepository<Expense, String> {

    List<Expense> findByUserOrderByDateDesc(User user);
}

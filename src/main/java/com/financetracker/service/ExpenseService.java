package com.financetracker.service;

import com.financetracker.model.Expense;
import com.financetracker.model.User;
import com.financetracker.repository.ExpenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ExpenseService {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Transactional
    public Expense addExpense(User user, BigDecimal amount, String description,
                              String sourceOfPayment, LocalDate date, String category) {
        Expense expense = new Expense();
        expense.setUser(user);
        expense.setAmount(amount);
        expense.setDescription(description);
        expense.setSourceOfPayment(sourceOfPayment);
        expense.setDate(date != null ? date : LocalDate.now());
        expense.setCategory(category);
        expense.setFromSplit(false);
        return expenseRepository.save(expense);
    }

    @Transactional
    public void deleteExpense(String expenseId, User currentUser) {
        Expense expense = expenseRepository.findById(expenseId)
            .orElseThrow(() -> new RuntimeException("Expense not found"));
        if (!expense.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Access denied: not your expense");
        }
        expenseRepository.delete(expense);
    }

    public Map<String, List<Expense>> getExpensesGroupedByMonth(User user) {
        List<Expense> expenses = expenseRepository.findByUserOrderByDateDesc(user);
        LinkedHashMap<String, List<Expense>> grouped = new LinkedHashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        for (Expense expense : expenses) {
            String key = expense.getDate().format(formatter);
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(expense);
        }
        return grouped;
    }

    public BigDecimal getTotalForMonth(List<Expense> expenses) {
        return expenses.stream()
            .map(Expense::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

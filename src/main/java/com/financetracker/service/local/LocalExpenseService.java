package com.financetracker.service.local;

import com.financetracker.model.Expense;
import com.financetracker.model.User;
import com.financetracker.model.local.LocalExpenseData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class LocalExpenseService {

    private static final String STORE = "expenses";

    @Autowired
    private LocalJsonStore store;

    public Expense addExpense(User user, BigDecimal amount, String description,
                              String sourceOfPayment, String sourceOfPurchase, LocalDate date, String category) {
        LocalExpenseData data = new LocalExpenseData();
        data.id = store.generateId();
        data.userId = user.getId();
        data.amount = amount;
        data.description = description;
        data.sourceOfPayment = sourceOfPayment;
        data.sourceOfPurchase = sourceOfPurchase;
        data.category = category;
        data.date = date != null ? date : LocalDate.now();
        data.fromSplit = false;
        store.save(STORE, data, LocalExpenseData.class);
        return toExpense(data, user);
    }

    public void deleteExpense(String expenseId, User currentUser) {
        List<LocalExpenseData> all = store.readAll(STORE, LocalExpenseData.class);
        LocalExpenseData data = all.stream()
            .filter(e -> expenseId.equals(e.id))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Expense not found"));
        if (!currentUser.getId().equals(data.userId)) {
            throw new RuntimeException("Access denied: not your expense");
        }
        store.deleteById(STORE, expenseId, LocalExpenseData.class);
    }

    public Map<String, List<Expense>> getExpensesGroupedByMonth(User user) {
        List<LocalExpenseData> all = store.readAll(STORE, LocalExpenseData.class);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        LinkedHashMap<String, List<Expense>> grouped = new LinkedHashMap<>();

        all.stream()
            .filter(e -> user.getId().equals(e.userId))
            .sorted(Comparator.comparing((LocalExpenseData e) -> e.date).reversed())
            .forEach(e -> {
                String key = e.date.format(formatter);
                grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(toExpense(e, user));
            });

        return grouped;
    }

    public BigDecimal getTotalForMonth(List<Expense> expenses) {
        return expenses.stream()
            .map(Expense::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Called by LocalSplitService when adding a split expense
    public void addSplitExpense(User participant, BigDecimal amount, String description,
                                 LocalDate date, String splitId, String category, String sourceOfPurchase) {
        LocalExpenseData data = new LocalExpenseData();
        data.id = store.generateId();
        data.userId = participant.getId();
        data.amount = amount;
        data.description = description;
        data.date = date;
        data.fromSplit = true;
        data.splitId = splitId;
        data.category = category;
        data.sourceOfPurchase = sourceOfPurchase;
        store.save(STORE, data, LocalExpenseData.class);
    }

    private Expense toExpense(LocalExpenseData d, User user) {
        Expense e = new Expense();
        e.setId(d.id);
        e.setUser(user);
        e.setAmount(d.amount);
        e.setDescription(d.description);
        e.setSourceOfPayment(d.sourceOfPayment);
        e.setSourceOfPurchase(d.sourceOfPurchase);
        e.setCategory(d.category);
        e.setDate(d.date);
        e.setFromSplit(d.fromSplit);
        return e;
    }
}

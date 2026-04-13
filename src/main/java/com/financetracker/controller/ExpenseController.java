package com.financetracker.controller;

import com.financetracker.model.User;
import com.financetracker.service.ExpenseService;
import com.financetracker.service.UserService;
import com.financetracker.service.local.LocalExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;

@Controller
@RequestMapping("/expenses")
public class ExpenseController {

    @Autowired
    private UserService userService;

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private LocalExpenseService localExpenseService;

    // ---- LOCAL FILE STORAGE (active) ----

    @PostMapping("/add")
    public String addExpense(
            @RequestParam BigDecimal amount,
            @RequestParam String description,
            @RequestParam(required = false) String sourceOfPayment,
            @RequestParam(required = false) String sourceOfPurchase,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String category,
            RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userService.getCurrentUser();
            localExpenseService.addExpense(currentUser, amount, description, sourceOfPayment, sourceOfPurchase, date, category);
            redirectAttributes.addFlashAttribute("successMessage", "Expense added successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to add expense: " + e.getMessage());
        }
        return "redirect:/dashboard?tab=expenses";
    }

    @PostMapping("/delete/{id}")
    public String deleteExpense(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userService.getCurrentUser();
            localExpenseService.deleteExpense(id, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Expense removed.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to remove expense: " + e.getMessage());
        }
        return "redirect:/dashboard?tab=expenses";
    }

    // ---- MONGODB (commented out — uncomment to switch back) ----

//    @PostMapping("/add")
//    public String addExpense(
//            @RequestParam BigDecimal amount,
//            @RequestParam String description,
//            @RequestParam(required = false) String sourceOfPayment,
//            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
//            @RequestParam(required = false) String category,
//            RedirectAttributes redirectAttributes) {
//        try {
//            User currentUser = userService.getCurrentUser();
//            expenseService.addExpense(currentUser, amount, description, sourceOfPayment, date, category);
//            redirectAttributes.addFlashAttribute("successMessage", "Expense added successfully.");
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("errorMessage", "Failed to add expense: " + e.getMessage());
//        }
//        return "redirect:/dashboard?tab=expenses";
//    }

//    @PostMapping("/delete/{id}")
//    public String deleteExpense(@PathVariable String id, RedirectAttributes redirectAttributes) {
//        try {
//            User currentUser = userService.getCurrentUser();
//            expenseService.deleteExpense(id, currentUser);
//            redirectAttributes.addFlashAttribute("successMessage", "Expense removed.");
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("errorMessage", "Failed to remove expense: " + e.getMessage());
//        }
//        return "redirect:/dashboard?tab=expenses";
//    }
}

package com.financetracker.controller;

import com.financetracker.model.Expense;
import com.financetracker.model.User;
import com.financetracker.service.ExpenseService;
import com.financetracker.service.SplitService;
import com.financetracker.service.UserService;
import com.financetracker.service.local.LocalExpenseService;
import com.financetracker.service.local.LocalSplitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    @Autowired
    private UserService userService;

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private SplitService splitService;

    @Autowired
    private LocalExpenseService localExpenseService;

    @Autowired
    private LocalSplitService localSplitService;

    @GetMapping("/")
    public String index() {
        return "redirect:/dashboard";
    }

    @GetMapping("/login")
    public String login(@RequestParam(required = false) String logout,
                        @RequestParam(required = false) String error,
                        Model model) {
        if (logout != null) model.addAttribute("message", "You have been logged out.");
        if (error != null)  model.addAttribute("error", "Login failed. Please try again.");
        return "login";
    }

    // ---- LOCAL FILE STORAGE (active) ----

    @GetMapping("/dashboard")
    public String dashboard(Model model,
                            @RequestParam(defaultValue = "expenses") String tab) {
        User currentUser = userService.getCurrentUser();
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("activeTab", tab);

        Map<String, List<Expense>> expensesByMonth = localExpenseService.getExpensesGroupedByMonth(currentUser);
        Map<String, BigDecimal> monthlyTotals = new LinkedHashMap<>();
        expensesByMonth.forEach((month, expenses) ->
            monthlyTotals.put(month, localExpenseService.getTotalForMonth(expenses)));

        model.addAttribute("expensesByMonth", expensesByMonth);
        model.addAttribute("monthlyTotals", monthlyTotals);
        model.addAttribute("splitGroups", localSplitService.getGroupsForUser(currentUser));
        model.addAttribute("pendingInvites", localSplitService.getPendingInvitesForUser(currentUser));

        return "dashboard";
    }

    // ---- MONGODB (commented out — uncomment to switch back) ----

//    @GetMapping("/dashboard")
//    public String dashboard(Model model,
//                            @RequestParam(defaultValue = "expenses") String tab) {
//        User currentUser = userService.getCurrentUser();
//        model.addAttribute("currentUser", currentUser);
//        model.addAttribute("activeTab", tab);
//
//        Map<String, List<Expense>> expensesByMonth = expenseService.getExpensesGroupedByMonth(currentUser);
//        Map<String, BigDecimal> monthlyTotals = new LinkedHashMap<>();
//        expensesByMonth.forEach((month, expenses) ->
//            monthlyTotals.put(month, expenseService.getTotalForMonth(expenses)));
//
//        model.addAttribute("expensesByMonth", expensesByMonth);
//        model.addAttribute("monthlyTotals", monthlyTotals);
//        model.addAttribute("splitGroups", splitService.getGroupsForUser(currentUser));
//        model.addAttribute("pendingInvites", splitService.getPendingInvitesForUser(currentUser));
//
//        return "dashboard";
//    }
}

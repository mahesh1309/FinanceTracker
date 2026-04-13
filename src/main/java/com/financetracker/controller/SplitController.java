package com.financetracker.controller;

import com.financetracker.model.SplitGroup;
import com.financetracker.model.User;
import com.financetracker.service.SplitService;
import com.financetracker.service.UserService;
import com.financetracker.service.local.LocalSplitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/splits")
public class SplitController {

    @Autowired
    private UserService userService;

    @Autowired
    private SplitService splitService;

    @Autowired
    private LocalSplitService localSplitService;

    // ---- LOCAL FILE STORAGE (active) ----

    @PostMapping("/groups/create")
    public String createGroup(@RequestParam String name,
                              RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userService.getCurrentUser();
            SplitGroup group = localSplitService.createGroup(name.trim(), currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Group '" + group.getName() + "' created.");
            return "redirect:/splits/groups/" + group.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create group: " + e.getMessage());
            return "redirect:/dashboard?tab=splits";
        }
    }

    @GetMapping("/groups/{id}")
    public String groupDetail(@PathVariable String id, Model model) {
        try {
            User currentUser = userService.getCurrentUser();
            SplitGroup group = localSplitService.getGroupDetail(id, currentUser);
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("group", group);
            model.addAttribute("balances", localSplitService.getBalanceSummary(group, currentUser));
            return "splits/group-detail";
        } catch (Exception e) {
            return "redirect:/dashboard?tab=splits";
        }
    }

    @PostMapping("/groups/{id}/members/add")
    public String addMember(@PathVariable String id,
                            @RequestParam String userId,
                            RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userService.getCurrentUser();
            localSplitService.addMemberToGroup(id, userId, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Invite sent successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to send invite: " + e.getMessage());
        }
        return "redirect:/splits/groups/" + id;
    }

    @PostMapping("/groups/{id}/exit")
    public String exitGroup(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userService.getCurrentUser();
            localSplitService.exitGroup(id, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "You have left the group.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to exit group: " + e.getMessage());
        }
        return "redirect:/dashboard?tab=splits";
    }

    @PostMapping("/groups/{id}/splits/add")
    public String addSplit(@PathVariable String id,
                           @RequestParam String description,
                           @RequestParam BigDecimal totalAmount,
                           @RequestParam(required = false) String category,
                           @RequestParam(required = false) String sourceOfPurchase,
                           @RequestParam Map<String, String> allParams,
                           RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userService.getCurrentUser();

            Map<String, BigDecimal> participantAmounts = new HashMap<>();
            for (Map.Entry<String, String> entry : allParams.entrySet()) {
                if (entry.getKey().startsWith("participant_")) {
                    String participantUserId = entry.getKey().replace("participant_", "");
                    String amountStr = entry.getValue();
                    if (amountStr != null && !amountStr.trim().isEmpty()) {
                        try {
                            BigDecimal amount = new BigDecimal(amountStr.trim());
                            if (amount.compareTo(BigDecimal.ZERO) > 0) {
                                participantAmounts.put(participantUserId, amount);
                            }
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
            }

            if (participantAmounts.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Please add at least one participant with an amount.");
                return "redirect:/splits/groups/" + id;
            }

            localSplitService.addSplit(id, description, totalAmount, participantAmounts, currentUser, category, sourceOfPurchase);
            redirectAttributes.addFlashAttribute("successMessage", "Split added and expenses updated for all participants.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to add split: " + e.getMessage());
        }
        return "redirect:/splits/groups/" + id;
    }

    @PostMapping("/groups/{id}/settle")
    public String settleBalance(@PathVariable String id,
                                @RequestParam String withUserId,
                                @RequestParam BigDecimal amount,
                                @RequestParam boolean youOwe,
                                RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userService.getCurrentUser();
            String fromUserId = youOwe ? currentUser.getId() : withUserId;
            String toUserId   = youOwe ? withUserId : currentUser.getId();
            localSplitService.recordSettlement(id, fromUserId, toUserId, amount);
            redirectAttributes.addFlashAttribute("successMessage", "Balance marked as settled.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to record settlement: " + e.getMessage());
        }
        return "redirect:/splits/groups/" + id;
    }

    @PostMapping("/invites/{id}/accept")
    public String acceptInvite(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userService.getCurrentUser();
            localSplitService.acceptInvite(id, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "You have joined the group.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to accept invite: " + e.getMessage());
        }
        return "redirect:/dashboard?tab=splits";
    }

    @PostMapping("/invites/{id}/reject")
    public String rejectInvite(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userService.getCurrentUser();
            localSplitService.rejectInvite(id, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Invite declined.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to decline invite: " + e.getMessage());
        }
        return "redirect:/dashboard?tab=splits";
    }

    @GetMapping("/users/search")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> searchUsers(@RequestParam String q) {
        try {
            User currentUser = userService.getCurrentUser();
            List<User> users = userService.searchUsers(q, currentUser.getEmail());
            List<Map<String, Object>> result = users.stream()
                .map(u -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", u.getId());
                    map.put("email", u.getEmail());
                    map.put("name", u.getName());
                    map.put("picture", u.getPicture());
                    return map;
                })
                .collect(Collectors.toList());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ---- MONGODB (commented out — uncomment to switch back) ----

//    @PostMapping("/groups/create")
//    public String createGroup(@RequestParam String name, RedirectAttributes redirectAttributes) {
//        try {
//            User currentUser = userService.getCurrentUser();
//            SplitGroup group = splitService.createGroup(name.trim(), currentUser);
//            redirectAttributes.addFlashAttribute("successMessage", "Group '" + group.getName() + "' created.");
//            return "redirect:/splits/groups/" + group.getId();
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create group: " + e.getMessage());
//            return "redirect:/dashboard?tab=splits";
//        }
//    }

//    @GetMapping("/groups/{id}")
//    public String groupDetail(@PathVariable String id, Model model) {
//        try {
//            User currentUser = userService.getCurrentUser();
//            SplitGroup group = splitService.getGroupDetail(id, currentUser);
//            model.addAttribute("currentUser", currentUser);
//            model.addAttribute("group", group);
//            model.addAttribute("balances", splitService.getBalanceSummary(group, currentUser));
//            return "splits/group-detail";
//        } catch (Exception e) {
//            return "redirect:/dashboard?tab=splits";
//        }
//    }

//    @PostMapping("/groups/{id}/members/add")
//    public String addMember(@PathVariable String id, @RequestParam String userId,
//                            RedirectAttributes redirectAttributes) {
//        try {
//            User currentUser = userService.getCurrentUser();
//            splitService.addMemberToGroup(id, userId, currentUser);
//            redirectAttributes.addFlashAttribute("successMessage", "Invite sent successfully.");
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("errorMessage", "Failed to send invite: " + e.getMessage());
//        }
//        return "redirect:/splits/groups/" + id;
//    }

//    @PostMapping("/groups/{id}/exit")
//    public String exitGroup(@PathVariable String id, RedirectAttributes redirectAttributes) {
//        try {
//            User currentUser = userService.getCurrentUser();
//            splitService.exitGroup(id, currentUser);
//            redirectAttributes.addFlashAttribute("successMessage", "You have left the group.");
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("errorMessage", "Failed to exit group: " + e.getMessage());
//        }
//        return "redirect:/dashboard?tab=splits";
//    }

//    @PostMapping("/groups/{id}/splits/add")
//    public String addSplit(@PathVariable String id, @RequestParam String description,
//                           @RequestParam BigDecimal totalAmount,
//                           @RequestParam(required = false) String category,
//                           @RequestParam Map<String, String> allParams,
//                           RedirectAttributes redirectAttributes) {
//        try {
//            User currentUser = userService.getCurrentUser();
//            Map<String, BigDecimal> participantAmounts = new HashMap<>();
//            for (Map.Entry<String, String> entry : allParams.entrySet()) {
//                if (entry.getKey().startsWith("participant_")) {
//                    String participantUserId = entry.getKey().replace("participant_", "");
//                    String amountStr = entry.getValue();
//                    if (amountStr != null && !amountStr.trim().isEmpty()) {
//                        try {
//                            BigDecimal amount = new BigDecimal(amountStr.trim());
//                            if (amount.compareTo(BigDecimal.ZERO) > 0) participantAmounts.put(participantUserId, amount);
//                        } catch (NumberFormatException ignored) {}
//                    }
//                }
//            }
//            if (participantAmounts.isEmpty()) {
//                redirectAttributes.addFlashAttribute("errorMessage", "Please add at least one participant with an amount.");
//                return "redirect:/splits/groups/" + id;
//            }
//            splitService.addSplit(id, description, totalAmount, participantAmounts, currentUser, category);
//            redirectAttributes.addFlashAttribute("successMessage", "Split added and expenses updated for all participants.");
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("errorMessage", "Failed to add split: " + e.getMessage());
//        }
//        return "redirect:/splits/groups/" + id;
//    }

//    @PostMapping("/invites/{id}/accept")
//    public String acceptInvite(@PathVariable String id, RedirectAttributes redirectAttributes) {
//        try {
//            User currentUser = userService.getCurrentUser();
//            splitService.acceptInvite(id, currentUser);
//            redirectAttributes.addFlashAttribute("successMessage", "You have joined the group.");
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("errorMessage", "Failed to accept invite: " + e.getMessage());
//        }
//        return "redirect:/dashboard?tab=splits";
//    }

//    @PostMapping("/invites/{id}/reject")
//    public String rejectInvite(@PathVariable String id, RedirectAttributes redirectAttributes) {
//        try {
//            User currentUser = userService.getCurrentUser();
//            splitService.rejectInvite(id, currentUser);
//            redirectAttributes.addFlashAttribute("successMessage", "Invite declined.");
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("errorMessage", "Failed to decline invite: " + e.getMessage());
//        }
//        return "redirect:/dashboard?tab=splits";
//    }
}

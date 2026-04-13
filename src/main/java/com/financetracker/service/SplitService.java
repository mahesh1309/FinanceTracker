package com.financetracker.service;

import com.financetracker.model.*;
import com.financetracker.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SplitService {

    @Autowired
    private SplitGroupRepository splitGroupRepository;

    @Autowired
    private SplitGroupMemberRepository splitGroupMemberRepository;

    @Autowired
    private SplitRepository splitRepository;

    @Autowired
    private SplitParticipantRepository splitParticipantRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private GroupInviteRepository groupInviteRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public SplitGroup createGroup(String name, User creator) {
        SplitGroup group = new SplitGroup();
        group.setName(name);
        group.setCreatedBy(creator);
        group.setCreatedAt(LocalDateTime.now());
        group = splitGroupRepository.save(group);

        SplitGroupMember member = new SplitGroupMember();
        member.setGroup(group);
        member.setUser(creator);
        member.setJoinedAt(LocalDateTime.now());
        member = splitGroupMemberRepository.save(member);

        group.getMembers().add(member);
        group = splitGroupRepository.save(group);

        return group;
    }

    @Transactional
    public GroupInvite addMemberToGroup(String groupId, String userId, User currentUser) {
        SplitGroup group = getGroupWithMemberCheck(groupId, currentUser);

        User userToAdd = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        if (splitGroupMemberRepository.existsByGroupAndUser(group, userToAdd)) {
            throw new RuntimeException("User is already a member of this group");
        }

        if (groupInviteRepository.existsByGroupAndInvitedUserAndStatus(group, userToAdd, "PENDING")) {
            throw new RuntimeException("An invite is already pending for this user");
        }

        GroupInvite invite = new GroupInvite();
        invite.setGroup(group);
        invite.setInvitedUser(userToAdd);
        invite.setInvitedBy(currentUser);
        invite.setStatus("PENDING");
        invite.setCreatedAt(LocalDateTime.now());
        return groupInviteRepository.save(invite);
    }

    @Transactional
    public void acceptInvite(String inviteId, User currentUser) {
        GroupInvite invite = groupInviteRepository.findById(inviteId)
            .orElseThrow(() -> new RuntimeException("Invite not found"));

        if (!invite.getInvitedUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Access denied");
        }
        if (!"PENDING".equals(invite.getStatus())) {
            throw new RuntimeException("Invite is no longer pending");
        }

        SplitGroup group = invite.getGroup();

        SplitGroupMember member = new SplitGroupMember();
        member.setGroup(group);
        member.setUser(currentUser);
        member.setJoinedAt(LocalDateTime.now());
        member = splitGroupMemberRepository.save(member);

        group.getMembers().add(member);
        splitGroupRepository.save(group);

        invite.setStatus("ACCEPTED");
        invite.setRespondedAt(LocalDateTime.now());
        groupInviteRepository.save(invite);
    }

    @Transactional
    public void rejectInvite(String inviteId, User currentUser) {
        GroupInvite invite = groupInviteRepository.findById(inviteId)
            .orElseThrow(() -> new RuntimeException("Invite not found"));

        if (!invite.getInvitedUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Access denied");
        }
        if (!"PENDING".equals(invite.getStatus())) {
            throw new RuntimeException("Invite is no longer pending");
        }

        invite.setStatus("REJECTED");
        invite.setRespondedAt(LocalDateTime.now());
        groupInviteRepository.save(invite);
    }

    public List<GroupInvite> getPendingInvitesForUser(User user) {
        return groupInviteRepository.findByInvitedUserAndStatus(user, "PENDING");
    }

    @Transactional
    public void exitGroup(String groupId, User currentUser) {
        SplitGroup group = splitGroupRepository.findById(groupId)
            .orElseThrow(() -> new RuntimeException("Group not found"));

        SplitGroupMember membership = splitGroupMemberRepository.findByGroupAndUser(group, currentUser)
            .orElseThrow(() -> new RuntimeException("You are not a member of this group"));

        group.getMembers().removeIf(m -> m.getId().equals(membership.getId()));
        splitGroupRepository.save(group);
        splitGroupMemberRepository.delete(membership);
    }

    @Transactional
    public Split addSplit(String groupId, String description, BigDecimal totalAmount,
                          Map<String, BigDecimal> participantAmounts, User currentUser, String category) {
        SplitGroup group = getGroupWithMemberCheck(groupId, currentUser);

        Split split = new Split();
        split.setGroup(group);
        split.setAddedBy(currentUser);
        split.setDescription(description);
        split.setCategory(category);
        split.setTotalAmount(totalAmount);
        split.setCreatedAt(LocalDateTime.now());
        split = splitRepository.save(split);

        for (Map.Entry<String, BigDecimal> entry : participantAmounts.entrySet()) {
            User participant = userRepository.findById(entry.getKey())
                .orElseThrow(() -> new RuntimeException("Participant user not found: " + entry.getKey()));

            BigDecimal participantAmount = entry.getValue();

            SplitParticipant splitParticipant = new SplitParticipant();
            splitParticipant.setSplit(split);
            splitParticipant.setUser(participant);
            splitParticipant.setAmount(participantAmount);
            splitParticipant = splitParticipantRepository.save(splitParticipant);

            split.getParticipants().add(splitParticipant);

            Expense expense = new Expense();
            expense.setUser(participant);
            expense.setAmount(participantAmount);
            expense.setDescription("Split: " + description + " (Group: " + group.getName() + ")");
            expense.setDate(LocalDate.now());
            expense.setFromSplit(true);
            expense.setSplit(split);
            expenseRepository.save(expense);
        }

        split = splitRepository.save(split);

        group.getSplits().add(split);
        splitGroupRepository.save(group);

        return split;
    }

    public List<SplitGroup> getGroupsForUser(User user) {
        return splitGroupMemberRepository.findByUser(user)
            .stream()
            .map(SplitGroupMember::getGroup)
            .collect(Collectors.toList());
    }

    public SplitGroup getGroupDetail(String groupId, User currentUser) {
        return getGroupWithMemberCheck(groupId, currentUser);
    }

    public List<Map<String, Object>> getBalanceSummary(SplitGroup group, User currentUser) {
        Map<String, BigDecimal> netByUserId = new LinkedHashMap<>();
        Map<String, User> usersById = new LinkedHashMap<>();

        for (Split split : group.getSplits()) {
            User paidBy = split.getAddedBy();
            for (SplitParticipant sp : split.getParticipants()) {
                User participantUser = sp.getUser();
                // Skip when payer is also a participant for their own share
                if (paidBy.getId().equals(participantUser.getId())) continue;

                if (paidBy.getId().equals(currentUser.getId())) {
                    // I paid — participant owes me
                    usersById.putIfAbsent(participantUser.getId(), participantUser);
                    netByUserId.merge(participantUser.getId(), sp.getAmount(), BigDecimal::add);
                } else if (participantUser.getId().equals(currentUser.getId())) {
                    // I'm a participant — I owe paidBy
                    usersById.putIfAbsent(paidBy.getId(), paidBy);
                    netByUserId.merge(paidBy.getId(), sp.getAmount().negate(), BigDecimal::add);
                }
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        netByUserId.forEach((userId, amount) -> {
            if (amount.compareTo(BigDecimal.ZERO) == 0) return;
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("user", usersById.get(userId));
            entry.put("amount", amount.abs());
            entry.put("youOwe", amount.compareTo(BigDecimal.ZERO) < 0);
            result.add(entry);
        });
        return result;
    }

    private SplitGroup getGroupWithMemberCheck(String groupId, User currentUser) {
        SplitGroup group = splitGroupRepository.findById(groupId)
            .orElseThrow(() -> new RuntimeException("Group not found"));

        if (!splitGroupMemberRepository.existsByGroupAndUser(group, currentUser)) {
            throw new RuntimeException("Access denied: you are not a member of this group");
        }
        return group;
    }
}

package com.financetracker.service.local;

import com.financetracker.model.*;
import com.financetracker.model.local.*;
import com.financetracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LocalSplitService {

    private static final String GROUPS      = "split_groups";
    private static final String MEMBERS     = "split_members";
    private static final String SPLITS      = "splits";
    private static final String PARTS       = "split_participants";
    private static final String INVITES     = "group_invites";
    private static final String SETTLEMENTS = "settlements";

    @Autowired private LocalJsonStore store;
    @Autowired private LocalExpenseService localExpenseService;
    @Autowired private UserRepository userRepository;

    // ---- Groups ----

    public SplitGroup createGroup(String name, User creator) {
        LocalSplitGroupData gd = new LocalSplitGroupData();
        gd.id = store.generateId();
        gd.name = name;
        gd.creatorId = creator.getId();
        gd.createdAt = LocalDateTime.now();
        store.save(GROUPS, gd, LocalSplitGroupData.class);

        LocalSplitGroupMemberData md = new LocalSplitGroupMemberData();
        md.id = store.generateId();
        md.groupId = gd.id;
        md.userId = creator.getId();
        md.joinedAt = LocalDateTime.now();
        store.save(MEMBERS, md, LocalSplitGroupMemberData.class);

        return buildGroup(gd.id);
    }

    public List<SplitGroup> getGroupsForUser(User user) {
        return store.readAll(MEMBERS, LocalSplitGroupMemberData.class).stream()
            .filter(m -> user.getId().equals(m.userId))
            .map(m -> buildGroup(m.groupId))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    public SplitGroup getGroupDetail(String groupId, User currentUser) {
        boolean isMember = store.readAll(MEMBERS, LocalSplitGroupMemberData.class).stream()
            .anyMatch(m -> groupId.equals(m.groupId) && currentUser.getId().equals(m.userId));
        if (!isMember) throw new RuntimeException("Access denied: you are not a member of this group");
        SplitGroup group = buildGroup(groupId);
        if (group == null) throw new RuntimeException("Group not found");
        return group;
    }

    public void exitGroup(String groupId, User currentUser) {
        List<LocalSplitGroupMemberData> members = store.readAll(MEMBERS, LocalSplitGroupMemberData.class);
        LocalSplitGroupMemberData membership = members.stream()
            .filter(m -> groupId.equals(m.groupId) && currentUser.getId().equals(m.userId))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("You are not a member of this group"));
        store.deleteById(MEMBERS, membership.id, LocalSplitGroupMemberData.class);
    }

    // ---- Invites ----

    public GroupInvite addMemberToGroup(String groupId, String userId, User currentUser) {
        boolean isMember = store.readAll(MEMBERS, LocalSplitGroupMemberData.class).stream()
            .anyMatch(m -> groupId.equals(m.groupId) && currentUser.getId().equals(m.userId));
        if (!isMember) throw new RuntimeException("Access denied: you are not a member of this group");

        User userToAdd = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        boolean alreadyMember = store.readAll(MEMBERS, LocalSplitGroupMemberData.class).stream()
            .anyMatch(m -> groupId.equals(m.groupId) && userId.equals(m.userId));
        if (alreadyMember) throw new RuntimeException("User is already a member of this group");

        boolean pendingExists = store.readAll(INVITES, LocalGroupInviteData.class).stream()
            .anyMatch(i -> groupId.equals(i.groupId) && userId.equals(i.invitedUserId) && "PENDING".equals(i.status));
        if (pendingExists) throw new RuntimeException("An invite is already pending for this user");

        LocalGroupInviteData id = new LocalGroupInviteData();
        id.id = store.generateId();
        id.groupId = groupId;
        id.invitedUserId = userId;
        id.invitedById = currentUser.getId();
        id.status = "PENDING";
        id.createdAt = LocalDateTime.now();
        store.save(INVITES, id, LocalGroupInviteData.class);

        return buildInvite(id);
    }

    public void acceptInvite(String inviteId, User currentUser) {
        LocalGroupInviteData inv = findInviteById(inviteId);
        if (!currentUser.getId().equals(inv.invitedUserId)) throw new RuntimeException("Access denied");
        if (!"PENDING".equals(inv.status)) throw new RuntimeException("Invite is no longer pending");

        LocalSplitGroupMemberData md = new LocalSplitGroupMemberData();
        md.id = store.generateId();
        md.groupId = inv.groupId;
        md.userId = currentUser.getId();
        md.joinedAt = LocalDateTime.now();
        store.save(MEMBERS, md, LocalSplitGroupMemberData.class);

        inv.status = "ACCEPTED";
        inv.respondedAt = LocalDateTime.now();
        store.save(INVITES, inv, LocalGroupInviteData.class);
    }

    public void rejectInvite(String inviteId, User currentUser) {
        LocalGroupInviteData inv = findInviteById(inviteId);
        if (!currentUser.getId().equals(inv.invitedUserId)) throw new RuntimeException("Access denied");
        if (!"PENDING".equals(inv.status)) throw new RuntimeException("Invite is no longer pending");

        inv.status = "REJECTED";
        inv.respondedAt = LocalDateTime.now();
        store.save(INVITES, inv, LocalGroupInviteData.class);
    }

    public List<GroupInvite> getPendingInvitesForUser(User user) {
        return store.readAll(INVITES, LocalGroupInviteData.class).stream()
            .filter(i -> user.getId().equals(i.invitedUserId) && "PENDING".equals(i.status))
            .map(this::buildInvite)
            .collect(Collectors.toList());
    }

    // ---- Splits ----

    public Split addSplit(String groupId, String description, BigDecimal totalAmount,
                          Map<String, BigDecimal> participantAmounts, User currentUser, String category, String sourceOfPurchase) {
        boolean isMember = store.readAll(MEMBERS, LocalSplitGroupMemberData.class).stream()
            .anyMatch(m -> groupId.equals(m.groupId) && currentUser.getId().equals(m.userId));
        if (!isMember) throw new RuntimeException("Access denied: you are not a member of this group");

        LocalSplitData sd = new LocalSplitData();
        sd.id = store.generateId();
        sd.groupId = groupId;
        sd.addedById = currentUser.getId();
        sd.description = description;
        sd.category = category;
        sd.sourceOfPurchase = sourceOfPurchase;
        sd.totalAmount = totalAmount;
        sd.createdAt = LocalDateTime.now();
        store.save(SPLITS, sd, LocalSplitData.class);

        SplitGroup group = buildGroupShallow(groupId);

        for (Map.Entry<String, BigDecimal> entry : participantAmounts.entrySet()) {
            User participant = userRepository.findById(entry.getKey())
                .orElseThrow(() -> new RuntimeException("Participant user not found: " + entry.getKey()));

            LocalSplitParticipantData pd = new LocalSplitParticipantData();
            pd.id = store.generateId();
            pd.splitId = sd.id;
            pd.userId = participant.getId();
            pd.amount = entry.getValue();
            store.save(PARTS, pd, LocalSplitParticipantData.class);

            localExpenseService.addSplitExpense(
                participant, entry.getValue(),
                "Split: " + description + " (Group: " + group.getName() + ")",
                LocalDate.now(), sd.id, category, sourceOfPurchase);
        }

        return buildSplit(sd);
    }

    // ---- Balance Summary ----

    public List<Map<String, Object>> getBalanceSummary(SplitGroup group, User currentUser) {
        Map<String, BigDecimal> netByUserId = new LinkedHashMap<>();
        Map<String, User> usersById = new LinkedHashMap<>();

        for (Split split : group.getSplits()) {
            User paidBy = split.getAddedBy();
            for (SplitParticipant sp : split.getParticipants()) {
                User participantUser = sp.getUser();
                if (paidBy.getId().equals(participantUser.getId())) continue;

                if (paidBy.getId().equals(currentUser.getId())) {
                    usersById.putIfAbsent(participantUser.getId(), participantUser);
                    netByUserId.merge(participantUser.getId(), sp.getAmount(), BigDecimal::add);
                } else if (participantUser.getId().equals(currentUser.getId())) {
                    usersById.putIfAbsent(paidBy.getId(), paidBy);
                    netByUserId.merge(paidBy.getId(), sp.getAmount().negate(), BigDecimal::add);
                }
            }
        }

        // Apply settlements: offset the net balance for each settled pair
        store.readAll(SETTLEMENTS, LocalSettlementData.class).stream()
            .filter(s -> group.getId().equals(s.groupId))
            .forEach(s -> {
                if (s.fromUserId.equals(currentUser.getId())) {
                    // currentUser paid toUser → reduce what currentUser owes
                    netByUserId.merge(s.toUserId, s.amount, BigDecimal::add);
                } else if (s.toUserId.equals(currentUser.getId())) {
                    // fromUser paid currentUser → reduce what fromUser owes
                    netByUserId.merge(s.fromUserId, s.amount.negate(), BigDecimal::add);
                }
            });

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

    public void recordSettlement(String groupId, String fromUserId, String toUserId, BigDecimal amount) {
        // Remove any existing settlement for this pair in this group before saving a new one
        store.readAll(SETTLEMENTS, LocalSettlementData.class).stream()
            .filter(s -> groupId.equals(s.groupId)
                    && s.fromUserId.equals(fromUserId)
                    && s.toUserId.equals(toUserId))
            .forEach(s -> store.deleteById(SETTLEMENTS, s.id, LocalSettlementData.class));

        LocalSettlementData s = new LocalSettlementData();
        s.id = store.generateId();
        s.groupId = groupId;
        s.fromUserId = fromUserId;
        s.toUserId = toUserId;
        s.amount = amount;
        s.settledAt = LocalDateTime.now();
        store.save(SETTLEMENTS, s, LocalSettlementData.class);
    }

    // ---- Builders ----

    private SplitGroup buildGroup(String groupId) {
        return store.readAll(GROUPS, LocalSplitGroupData.class).stream()
            .filter(g -> groupId.equals(g.id))
            .findFirst()
            .map(gd -> {
                SplitGroup group = new SplitGroup();
                group.setId(gd.id);
                group.setName(gd.name);
                group.setCreatedAt(gd.createdAt);
                group.setCreatedBy(getUserById(gd.creatorId));

                List<SplitGroupMember> members = store.readAll(MEMBERS, LocalSplitGroupMemberData.class).stream()
                    .filter(m -> groupId.equals(m.groupId))
                    .map(m -> {
                        SplitGroupMember sgm = new SplitGroupMember();
                        sgm.setId(m.id);
                        sgm.setGroup(group);
                        sgm.setUser(getUserById(m.userId));
                        sgm.setJoinedAt(m.joinedAt);
                        return sgm;
                    })
                    .collect(Collectors.toList());
                group.setMembers(members);

                List<Split> splits = store.readAll(SPLITS, LocalSplitData.class).stream()
                    .filter(s -> groupId.equals(s.groupId))
                    .map(this::buildSplit)
                    .collect(Collectors.toList());
                group.setSplits(splits);

                return group;
            })
            .orElse(null);
    }

    /** Builds a SplitGroup with name only (no members/splits) — used when group name is needed without full assembly. */
    private SplitGroup buildGroupShallow(String groupId) {
        return store.readAll(GROUPS, LocalSplitGroupData.class).stream()
            .filter(g -> groupId.equals(g.id))
            .findFirst()
            .map(gd -> {
                SplitGroup group = new SplitGroup();
                group.setId(gd.id);
                group.setName(gd.name);
                group.setCreatedAt(gd.createdAt);
                group.setCreatedBy(getUserById(gd.creatorId));
                group.setMembers(new ArrayList<>());
                group.setSplits(new ArrayList<>());
                return group;
            })
            .orElseThrow(() -> new RuntimeException("Group not found"));
    }

    private Split buildSplit(LocalSplitData sd) {
        Split split = new Split();
        split.setId(sd.id);
        split.setAddedBy(getUserById(sd.addedById));
        split.setDescription(sd.description);
        split.setCategory(sd.category);
        split.setSourceOfPurchase(sd.sourceOfPurchase);
        split.setTotalAmount(sd.totalAmount);
        split.setCreatedAt(sd.createdAt);

        List<SplitParticipant> participants = store.readAll(PARTS, LocalSplitParticipantData.class).stream()
            .filter(p -> sd.id.equals(p.splitId))
            .map(p -> {
                SplitParticipant sp = new SplitParticipant();
                sp.setId(p.id);
                sp.setSplit(split);
                sp.setUser(getUserById(p.userId));
                sp.setAmount(p.amount);
                return sp;
            })
            .collect(Collectors.toList());
        split.setParticipants(participants);

        return split;
    }

    private GroupInvite buildInvite(LocalGroupInviteData i) {
        GroupInvite inv = new GroupInvite();
        inv.setId(i.id);
        inv.setStatus(i.status);
        inv.setCreatedAt(i.createdAt);
        inv.setRespondedAt(i.respondedAt);
        inv.setInvitedUser(getUserById(i.invitedUserId));
        inv.setInvitedBy(getUserById(i.invitedById));
        inv.setGroup(buildGroupShallow(i.groupId));
        return inv;
    }

    private LocalGroupInviteData findInviteById(String inviteId) {
        return store.readAll(INVITES, LocalGroupInviteData.class).stream()
            .filter(i -> inviteId.equals(i.id))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Invite not found"));
    }

    private User getUserById(String userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found: " + userId));
    }
}

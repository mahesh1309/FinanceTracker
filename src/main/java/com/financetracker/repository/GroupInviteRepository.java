package com.financetracker.repository;

import com.financetracker.model.GroupInvite;
import com.financetracker.model.SplitGroup;
import com.financetracker.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface GroupInviteRepository extends MongoRepository<GroupInvite, String> {

    List<GroupInvite> findByInvitedUserAndStatus(User invitedUser, String status);

    boolean existsByGroupAndInvitedUserAndStatus(SplitGroup group, User invitedUser, String status);
}

package com.financetracker.repository;

import com.financetracker.model.SplitGroup;
import com.financetracker.model.SplitGroupMember;
import com.financetracker.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SplitGroupMemberRepository extends MongoRepository<SplitGroupMember, String> {

    Optional<SplitGroupMember> findByGroupAndUser(SplitGroup group, User user);

    boolean existsByGroupAndUser(SplitGroup group, User user);

    List<SplitGroupMember> findByUser(User user);
}

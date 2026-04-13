package com.financetracker.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "group_invites")
@Getter
@Setter
@NoArgsConstructor
public class GroupInvite {

    @Id
    private String id;

    @DBRef
    private SplitGroup group;

    @DBRef
    private User invitedUser;

    @DBRef
    private User invitedBy;

    private String status; // "PENDING", "ACCEPTED", "REJECTED"

    private LocalDateTime createdAt;

    private LocalDateTime respondedAt;
}

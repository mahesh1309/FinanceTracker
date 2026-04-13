package com.financetracker.model.local;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LocalGroupInviteData implements HasId {
    public String id;
    public String groupId;
    public String invitedUserId;
    public String invitedById;
    public String status;
    public LocalDateTime createdAt;
    public LocalDateTime respondedAt;

    @Override
    public String getId() { return id; }
}

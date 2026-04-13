package com.financetracker.model.local;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LocalSplitGroupMemberData implements HasId {
    public String id;
    public String groupId;
    public String userId;
    public LocalDateTime joinedAt;

    @Override
    public String getId() { return id; }
}

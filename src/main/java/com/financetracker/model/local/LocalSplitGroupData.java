package com.financetracker.model.local;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LocalSplitGroupData implements HasId {
    public String id;
    public String name;
    public String creatorId;
    public LocalDateTime createdAt;

    @Override
    public String getId() { return id; }
}

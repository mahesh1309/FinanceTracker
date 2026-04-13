package com.financetracker.model.local;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LocalSplitData implements HasId {
    public String id;
    public String groupId;
    public String addedById;
    public String description;
    public String category;
    public String sourceOfPurchase;
    public BigDecimal totalAmount;
    public LocalDateTime createdAt;

    @Override
    public String getId() { return id; }
}

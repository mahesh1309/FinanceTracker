package com.financetracker.model.local;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LocalSplitParticipantData implements HasId {
    public String id;
    public String splitId;
    public String userId;
    public BigDecimal amount;

    @Override
    public String getId() { return id; }
}

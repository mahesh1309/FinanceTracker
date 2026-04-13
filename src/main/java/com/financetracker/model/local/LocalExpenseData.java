package com.financetracker.model.local;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LocalExpenseData implements HasId {
    public String id;
    public String userId;
    public BigDecimal amount;
    public String description;
    public String sourceOfPayment;
    public String sourceOfPurchase;
    public String category;
    public LocalDate date;
    public boolean fromSplit;
    public String splitId;

    @Override
    public String getId() { return id; }
}

package com.financetracker.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;

@Document(collection = "expenses")
@Getter
@Setter
@NoArgsConstructor
public class Expense {

    @Id
    private String id;

    @DBRef
    @Indexed
    private User user;

    private BigDecimal amount;

    private String description;

    private String sourceOfPayment;

    private String sourceOfPurchase;

    private LocalDate date;

    private String category;

    private boolean fromSplit = false;

    @DBRef
    private Split split;
}

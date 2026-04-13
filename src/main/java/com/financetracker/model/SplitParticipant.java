package com.financetracker.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Document(collection = "split_participants")
@Getter
@Setter
@NoArgsConstructor
public class SplitParticipant {

    @Id
    private String id;

    @DBRef
    private Split split;

    @DBRef
    private User user;

    private BigDecimal amount;
}

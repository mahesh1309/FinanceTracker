package com.financetracker.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "splits")
@Getter
@Setter
@NoArgsConstructor
public class Split {

    @Id
    private String id;

    @DBRef
    private SplitGroup group;

    @DBRef
    private User addedBy;

    private BigDecimal totalAmount;

    private String description;

    private String category;

    private String sourceOfPurchase;

    private LocalDateTime createdAt;

    @DBRef
    private List<SplitParticipant> participants = new ArrayList<>();
}

package com.financetracker.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "split_groups")
@Getter
@Setter
@NoArgsConstructor
public class SplitGroup {

    @Id
    private String id;

    private String name;

    @DBRef
    private User createdBy;

    private LocalDateTime createdAt;

    @DBRef
    private List<SplitGroupMember> members = new ArrayList<>();

    @DBRef
    private List<Split> splits = new ArrayList<>();
}

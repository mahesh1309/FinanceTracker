package com.financetracker.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "split_group_members")
@CompoundIndex(def = "{'group.$id': 1, 'user.$id': 1}", unique = true)
@Getter
@Setter
@NoArgsConstructor
public class SplitGroupMember {

    @Id
    private String id;

    @DBRef
    private SplitGroup group;

    @DBRef
    private User user;

    private LocalDateTime joinedAt;
}

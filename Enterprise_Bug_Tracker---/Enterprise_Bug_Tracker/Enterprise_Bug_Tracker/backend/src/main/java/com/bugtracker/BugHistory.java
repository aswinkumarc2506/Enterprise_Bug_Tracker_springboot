package com.bugtracker;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class BugHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    private Bug bug;
    
    private String changedBy;
    private String fieldName;
    private String oldValue;
    private String newValue;
    private Long changedAt;
    
    public BugHistory(Bug bug, String changedBy, String fieldName, String oldValue, String newValue) {
        this.bug = bug;
        this.changedBy = changedBy;
        this.fieldName = fieldName;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.changedAt = System.currentTimeMillis();
    }
}

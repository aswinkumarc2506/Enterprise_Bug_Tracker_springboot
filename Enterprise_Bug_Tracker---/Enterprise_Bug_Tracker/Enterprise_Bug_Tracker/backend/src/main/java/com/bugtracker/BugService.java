package com.bugtracker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BugService {
    
    @Autowired
    private BugRepository bugRepository;
    
    @Autowired
    private BugHistoryRepository historyRepository;
    
    public void recordHistory(Bug bug, String changedBy, String fieldName, String oldValue, String newValue) {
        BugHistory history = new BugHistory(bug, changedBy, fieldName, oldValue, newValue);
        historyRepository.save(history);
    }
    
    public List<BugHistory> getBugHistory(Long bugId) {
        return historyRepository.findByBugIdOrderByChangedAtDesc(bugId);
    }
    
    public Map<String, Long> getDashboardStats() {
        List<Bug> allBugs = bugRepository.findAll();
        
        return Map.of(
            "TOTAL", (long) allBugs.size(),
            "OPEN", allBugs.stream().filter(b -> b.getStatus() == Bug.Status.OPEN).count(),
            "IN_PROGRESS", allBugs.stream().filter(b -> b.getStatus() == Bug.Status.IN_PROGRESS).count(),
            "RESOLVED", allBugs.stream().filter(b -> b.getStatus() == Bug.Status.RESOLVED).count(),
            "CLOSED", allBugs.stream().filter(b -> b.getStatus() == Bug.Status.CLOSED).count(),
            "CRITICAL", allBugs.stream().filter(b -> b.getPriority() == Bug.Priority.CRITICAL).count(),
            "HIGH", allBugs.stream().filter(b -> b.getPriority() == Bug.Priority.HIGH).count()
        );
    }
    
    public List<Bug> getBugsByAssignee(String assignee) {
        return bugRepository.findAll().stream()
            .filter(b -> assignee.equals(b.getAssignedTo()))
            .collect(Collectors.toList());
    }
    
    public List<Bug> getBugsByStatus(Bug.Status status) {
        return bugRepository.findAll().stream()
            .filter(b -> b.getStatus() == status)
            .collect(Collectors.toList());
    }
}

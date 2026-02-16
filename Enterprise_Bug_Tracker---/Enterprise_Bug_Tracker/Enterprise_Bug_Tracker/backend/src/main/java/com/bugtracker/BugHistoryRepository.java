package com.bugtracker;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BugHistoryRepository extends JpaRepository<BugHistory, Long> {
    @Query("SELECT h FROM BugHistory h WHERE h.bug.id = :bugId ORDER BY h.changedAt DESC")
    List<BugHistory> findByBugIdOrderByChangedAtDesc(Long bugId);
}

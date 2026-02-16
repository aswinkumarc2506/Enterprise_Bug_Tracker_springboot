
package com.bugtracker;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.Map;

@Controller
public class BugController {

    @Autowired
    private BugRepository bugRepository;
    
    @Autowired
    private BugService bugService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUser = auth.getName();
        
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("stats", bugService.getDashboardStats());
        model.addAttribute("bugs", bugRepository.findAll());
        
        return "dashboard";
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }

    @PostMapping("/add")
    public String addBug(@RequestParam String title, 
                        @RequestParam String description,
                        @RequestParam String assignedTo,
                        @RequestParam(defaultValue = "MEDIUM") String priority) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUser = auth.getName();
        
        Bug bug = new Bug(title, description, currentUser);
        bug.setAssignedTo(assignedTo);
        bug.setPriority(Bug.Priority.valueOf(priority));
        
        Bug savedBug = bugRepository.save(bug);
        
        // Record history
        bugService.recordHistory(savedBug, currentUser, "STATUS", null, "OPEN");
        
        return "redirect:/dashboard";
    }

    @PostMapping("/update/{id}")
    public String updateBug(@PathVariable Long id,
                           @RequestParam(required = false) String status,
                           @RequestParam(required = false) String assignedTo,
                           @RequestParam(required = false) String resolution,
                           @RequestParam(required = false) String priority) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String currentUser = auth.getName();
            
            Bug bug = bugRepository.findById(id).orElse(null);
            if (bug != null) {
                // Record history for each change
                if (status != null && !status.isEmpty() && !status.equals(bug.getStatus().toString())) {
                    bugService.recordHistory(bug, currentUser, "STATUS", bug.getStatus().toString(), status);
                    bug.setStatus(Bug.Status.valueOf(status));
                }
                
                if (assignedTo != null && !assignedTo.isEmpty() && !assignedTo.equals(bug.getAssignedTo())) {
                    bugService.recordHistory(bug, currentUser, "ASSIGNEE", bug.getAssignedTo(), assignedTo);
                    bug.setAssignedTo(assignedTo);
                }
                
                if (resolution != null && !resolution.isEmpty() && !resolution.equals(bug.getResolution())) {
                    bugService.recordHistory(bug, currentUser, "RESOLUTION", bug.getResolution(), resolution);
                    bug.setResolution(resolution);
                }
                
                if (priority != null && !priority.isEmpty() && !priority.equals(bug.getPriority().toString())) {
                    bugService.recordHistory(bug, currentUser, "PRIORITY", bug.getPriority().toString(), priority);
                    bug.setPriority(Bug.Priority.valueOf(priority));
                }
                
                bug.setUpdatedAt(System.currentTimeMillis());
                bugRepository.save(bug);
                return "redirect:/bug/" + id;
            }
        } catch (Exception e) {
            System.err.println("Error updating bug: " + e.getMessage());
            e.printStackTrace();
        }
        return "redirect:/dashboard";
    }

    @GetMapping("/bug/{id}")
    public String bugDetail(@PathVariable Long id, Model model) {
        try {
            Bug bug = bugRepository.findById(id).orElse(null);
            if (bug != null) {
                List<BugHistory> history = bugService.getBugHistory(id);
                if (history == null) {
                    history = java.util.Collections.emptyList();
                }
                model.addAttribute("bug", bug);
                model.addAttribute("history", history);
                return "bug-detail";
            }
        } catch (Exception e) {
            System.err.println("Error retrieving bug details: " + e.getMessage());
            e.printStackTrace();
        }
        return "redirect:/dashboard";
    }

    @DeleteMapping("/delete/{id}")
    @ResponseBody
    public Map<String, String> deleteBug(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User.Role role = null;
        
        bugRepository.deleteById(id);
        return Map.of("status", "success", "message", "Bug deleted successfully");
    }

    @GetMapping("/api/dashboard-stats")
    @ResponseBody
    public Map<String, Long> getDashboardStats() {
        return bugService.getDashboardStats();
    }

    @GetMapping("/api/bugs/status/{status}")
    @ResponseBody
    public List<Bug> getBugsByStatus(@PathVariable String status) {
        return bugService.getBugsByStatus(Bug.Status.valueOf(status));
    }

    @GetMapping("/api/bugs/assignee/{assignee}")
    @ResponseBody
    public List<Bug> getBugsByAssignee(@PathVariable String assignee) {
        return bugService.getBugsByAssignee(assignee);
    }
}

package com.bugtracker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private BugRepository bugRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @GetMapping
    public String adminPanel(Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            
            List<User> users = userRepository.findAll();
            long totalBugs = bugRepository.count();
            model.addAttribute("users", users);
            model.addAttribute("totalBugs", totalBugs);
            model.addAttribute("totalUsers", users.size());
            model.addAttribute("isAdmin", isAdmin);
            model.addAttribute("currentUser", auth.getName());
        } catch (Exception e) {
            System.err.println("Error loading admin panel: " + e.getMessage());
            e.printStackTrace();
        }
        return "admin-panel";
    }
    
    @PostMapping("/user/add")
    public String addUser(@RequestParam String username,
                         @RequestParam String password,
                         @RequestParam String email,
                         @RequestParam String role) {
        try {
            User user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(password));
            user.setEmail(email);
            user.setRole(User.Role.valueOf(role));
            user.setEnabled(true);
            
            userRepository.save(user);
            System.out.println("User " + username + " created successfully with role " + role);
        } catch (Exception e) {
            System.err.println("Error creating user: " + e.getMessage());
            e.printStackTrace();
        }
        return "redirect:/admin";
    }
    
    @PostMapping("/user/{id}/update-role")
    public String updateUserRole(@PathVariable Long id,
                                @RequestParam String role) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            user.setRole(User.Role.valueOf(role));
            userRepository.save(user);
        }
        return "redirect:/admin";
    }
    
    @PostMapping("/user/{id}/disable")
    public String disableUser(@PathVariable Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            user.setEnabled(false);
            userRepository.save(user);
        }
        return "redirect:/admin";
    }
    
    @GetMapping("/reports")
    public String reports(Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            
            long totalBugs = bugRepository.count();
            List<Bug> allBugs = bugRepository.findAll();
            
            long openBugs = allBugs.stream().filter(b -> b.getStatus() == Bug.Status.OPEN).count();
            long resolvedBugs = allBugs.stream().filter(b -> b.getStatus() == Bug.Status.RESOLVED).count();
            long closedBugs = allBugs.stream().filter(b -> b.getStatus() == Bug.Status.CLOSED).count();
            
            model.addAttribute("totalBugs", totalBugs);
            model.addAttribute("openBugs", openBugs);
            model.addAttribute("resolvedBugs", resolvedBugs);
            model.addAttribute("closedBugs", closedBugs);
            model.addAttribute("bugs", allBugs != null ? allBugs : java.util.Collections.emptyList());
            model.addAttribute("currentUser", auth.getName());
            
            return "admin-reports";
        } catch (Exception e) {
            System.err.println("Error retrieving reports: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error loading reports. Please try again later.");
            return "admin-reports";
        }
    }
}

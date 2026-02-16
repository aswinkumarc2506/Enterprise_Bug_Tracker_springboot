package com.bugtracker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements ApplicationRunner {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private BugRepository bugRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
        try {
            // Initialize demo users if they don't exist
            if (userRepository.findByUsername("admin") == null) {
                User admin = new User("admin", passwordEncoder.encode("admin123"), "admin@bugtracker.com", User.Role.ADMIN);
                userRepository.save(admin);
                System.out.println("Created admin user");
            }
            
            if (userRepository.findByUsername("manager") == null) {
                User manager = new User("manager", passwordEncoder.encode("manager123"), "manager@bugtracker.com", User.Role.MANAGER);
                userRepository.save(manager);
                System.out.println("Created manager user");
            }
            
            if (userRepository.findByUsername("dev") == null) {
                User dev = new User("dev", passwordEncoder.encode("dev123"), "dev@bugtracker.com", User.Role.DEVELOPER);
                userRepository.save(dev);
                System.out.println("Created dev user");
            }
            
            if (userRepository.findByUsername("tester") == null) {
                User tester = new User("tester", passwordEncoder.encode("tester123"), "tester@bugtracker.com", User.Role.VIEWER);
                userRepository.save(tester);
                System.out.println("Created tester user");
            }
            
            // Initialize sample bugs
            if (bugRepository.count() == 0) {
                Bug bug1 = new Bug("Login page crashes on mobile", 
                    "The login page displays incorrectly on mobile devices and throws a JavaScript error.",
                    "admin");
                bug1.setPriority(Bug.Priority.CRITICAL);
                bug1.setAssignedTo("dev");
                bug1.setStatus(Bug.Status.IN_PROGRESS);
                bugRepository.save(bug1);
                
                Bug bug2 = new Bug("Database connection timeout",
                    "Database connections timeout after 30 seconds of inactivity.",
                    "admin");
                bug2.setPriority(Bug.Priority.HIGH);
                bug2.setAssignedTo("dev");
                bug2.setStatus(Bug.Status.OPEN);
                bugRepository.save(bug2);
                
                Bug bug3 = new Bug("Update profile feature not working",
                    "Users cannot update their profile information through the settings page.",
                    "manager");
                bug3.setPriority(Bug.Priority.MEDIUM);
                bug3.setAssignedTo("dev");
                bug3.setStatus(Bug.Status.OPEN);
                bugRepository.save(bug3);
                
                System.out.println("Sample bugs created");
            }
        } catch (Exception e) {
            System.err.println("Error during data initialization: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

package com.examportal.config;

import com.examportal.model.*;
import com.examportal.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Value;

/** Creates the default administrator once, without a proxied @Configuration class. */
@Component
public class AdminSeeder implements CommandLineRunner {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final boolean resetDefaultPassword;

    public AdminSeeder(UserRepository users, PasswordEncoder encoder,
                       @Value("${app.seed.reset-admin-password:false}") boolean resetDefaultPassword) {
        this.users = users;
        this.encoder = encoder;
        this.resetDefaultPassword = resetDefaultPassword;
    }

    @Override
    public void run(String... args) {
        if (!users.existsByEmail("admin@gmail.com")) {
            users.save(new User("admin@gmail.com", "System Administrator", encoder.encode("admin"), Role.ADMIN));
        } else if (resetDefaultPassword) {
            User admin = users.findByEmail("admin@gmail.com").orElseThrow();
            admin.setPassword(encoder.encode("admin"));
            admin.setName("System Administrator");
            admin.setRole(Role.ADMIN);
            users.save(admin);
        }
    }
}

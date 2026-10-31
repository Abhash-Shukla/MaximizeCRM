package com.maximizecrm.config;

import com.maximizecrm.entity.Role;
import com.maximizecrm.entity.User;
import com.maximizecrm.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Development-only seed mechanism to create an initial ADMIN user.
 *
 * IMPORTANT NOTE:
 * This account is intended strictly for local development and testing purposes.
 * Credentials (admin@maximizecrm.local / Admin@123) MUST be changed or disabled
 * prior to deploying this application to any staging or production environment.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        final String adminEmail = "admin@maximizecrm.local";

        if (!userRepository.existsByEmailIgnoreCase(adminEmail)) {
            User admin = new User();
            admin.setFullName("System Administrator");
            admin.setEmail(adminEmail);
            admin.setPasswordHash(passwordEncoder.encode("Admin@123"));
            admin.setRole(Role.ADMIN);
            admin.setActive(true);

            userRepository.save(admin);
            log.info("=================================================================");
            log.info("[DEV SEED] Initialized development admin account: {}", adminEmail);
            log.info("[DEV SEED] Password: Admin@123 (CHANGE IN PRODUCTION)");
            log.info("=================================================================");
        } else {
            log.info("[DEV SEED] Development admin account already exists: {}", adminEmail);
        }

        final String repEmail = "rep@maximizecrm.local";
        if (!userRepository.existsByEmailIgnoreCase(repEmail)) {
            User rep = new User();
            rep.setFullName("Sarah Connor");
            rep.setEmail(repEmail);
            rep.setPasswordHash(passwordEncoder.encode("Rep@123"));
            rep.setRole(Role.SALES_REP);
            rep.setActive(true);

            userRepository.save(rep);
            log.info("=================================================================");
            log.info("[DEV SEED] Initialized development sales rep account: {}", repEmail);
            log.info("[DEV SEED] Password: Rep@123 (CHANGE IN PRODUCTION)");
            log.info("=================================================================");
        }
    }
}


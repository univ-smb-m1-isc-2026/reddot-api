package com.reddot.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.reddot.api.entity.HelloMessage;
import com.reddot.api.entity.User;
import com.reddot.api.repository.HelloMessageRepository;
import com.reddot.api.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final HelloMessageRepository repo;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.password:admin}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        if (repo.count() == 0) {
            HelloMessage msg = new HelloMessage();
            msg.setMessage("Hello from Reddot! 🔴");
            repo.save(msg);
        }

        if (!userRepository.existsByUsername("admin")) {
            userRepository.save(User.builder()
                    .username("admin")
                    .email("admin@reddot.com")
                    .password(passwordEncoder.encode(adminPassword))
                    .role(User.Role.ADMIN)
                    .build());
        }
    }
}
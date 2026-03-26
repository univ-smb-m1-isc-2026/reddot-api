package com.reddot.api.config;

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

    @Override
    public void run(String... args) {
        if (repo.count() == 0) {
            HelloMessage msg = new HelloMessage();
            msg.setMessage("Hello from Reddot! 🔴");
            repo.save(msg);
        }

        if (userRepository.count() == 0) {
            User admin = User.builder()
                    .username("admin")
                    .email("admin@reddot.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role(User.Role.ADMIN)
                    .build();
            userRepository.save(admin);
        }
    }
}
package com.reddot.api.config;

import com.reddot.api.entity.HelloMessage;
import com.reddot.api.repository.HelloMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final HelloMessageRepository repo;

    @Override
    public void run(String... args) {
        if (repo.count() == 0) {
            HelloMessage msg = new HelloMessage();
            msg.setMessage("Test from Reddot!");
            repo.save(msg);
        }
    }
}
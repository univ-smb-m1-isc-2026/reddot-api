package com.reddot.api.controller;

import com.reddot.api.repository.HelloMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class HelloController {

    private final HelloMessageRepository repo;

    @GetMapping("/hello")
    public String hello() {
        return repo.findAll()
                .stream()
                .findFirst()
                .map(m -> m.getMessage())
                .orElse("No message in DB yet!");
    }
}
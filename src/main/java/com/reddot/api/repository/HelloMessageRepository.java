package com.reddot.api.repository;

import com.reddot.api.entity.HelloMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HelloMessageRepository extends JpaRepository<HelloMessage, Long> {
}
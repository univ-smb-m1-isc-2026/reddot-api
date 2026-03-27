package com.reddot.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reddot.api.entity.Vote;
import com.reddot.api.entity.VoteId;

public interface VoteRepository extends JpaRepository<Vote, VoteId> {
}
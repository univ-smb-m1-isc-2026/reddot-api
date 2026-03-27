package com.reddot.api.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VoteRequest {
    private int value; // +1 ou -1
}
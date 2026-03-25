package com.reddot.api.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VoteRequest {
    private short value; // +1 ou -1
}
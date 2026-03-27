package com.reddot.api.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TopicRequest {
    private String title;
    private String description;
}
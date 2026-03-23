package com.reddot.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ApiApplicationTests {

    @Test
    void mainClassExists() {
        assertDoesNotThrow(() -> Class.forName("com.reddot.api.ApiApplication"));
    }

}
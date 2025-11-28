package org.example.userservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
public class TestController {
    @GetMapping("/ping")
    public String ping() {
        return "User Service is running!";
    }
}

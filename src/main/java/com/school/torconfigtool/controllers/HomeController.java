package com.school.torconfigtool.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        // Add any necessary data to the model
        return "home"; // "index" corresponds to your Thymeleaf template file (e.g., index.html)
    }
}

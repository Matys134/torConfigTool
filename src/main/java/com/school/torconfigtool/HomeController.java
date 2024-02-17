package com.school.torconfigtool;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        // Add any necessary data to the model
        return "home.html"; // "index" corresponds to your Thymeleaf template file (e.g., index.html)
    }

    @GetMapping("/setup")
    public String setup() {
        // Add any necessary data to the model
        return "setup"; // "index" corresponds to your Thymeleaf template file (e.g., index.html)
    }
}

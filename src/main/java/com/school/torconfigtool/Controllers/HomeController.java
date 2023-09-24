package com.school.torconfigtool.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model) {
        // Add any necessary data to the model
        return "home.html"; // "index" corresponds to your Thymeleaf template file (e.g., index.html)
    }

    @GetMapping("/relay")
    public String relay(Model model) {
        // Add any necessary data to the model
        return "relay.html"; // "index" corresponds to your Thymeleaf template file (e.g., index.html)
    }

    @GetMapping("/hidden-services")
    public String hiddenService(Model model) {
        // Add any necessary data to the model
        return "hidden-service.html"; // "index" corresponds to your Thymeleaf template file (e.g., index.html)
    }

    @GetMapping("/proxy")
    public String proxy(Model model) {
        // Add any necessary data to the model
        return "proxy.html"; // "index" corresponds to your Thymeleaf template file (e.g., index.html)
    }
}

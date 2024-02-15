package com.school.torconfigtool.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * HomeController is a Spring MVC Controller that handles HTTP GET requests for the application's home and setup pages.
 */
@Controller
public class HomeController {

    /**
     * Handles HTTP GET requests for the application's home page.
     *
     * @return a String that represents the name of the Thymeleaf template to be rendered. In this case, "home".
     */
    @GetMapping("/")
    public String home() {
        // Add any necessary data to the model
        return "home"; // "home" corresponds to your Thymeleaf template file (e.g., home.html)
    }

    /**
     * Handles HTTP GET requests for the application's setup page.
     *
     * @return a String that represents the name of the Thymeleaf template to be rendered. In this case, "setup".
     */
    @GetMapping("/setup")
    public String setup() {
        // Add any necessary data to the model
        return "setup"; // "setup" corresponds to your Thymeleaf template file (e.g., setup.html)
    }
}
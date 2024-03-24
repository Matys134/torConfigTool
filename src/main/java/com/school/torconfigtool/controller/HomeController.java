package com.school.torconfigtool.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * HomeController class handles the routing for the home and setup pages.
 */
@Controller
public class HomeController {

    /**
     * Handles GET requests to the base endpoint ("/").
     * Returns the home view.
     *
     * @return the name of the home view
     */
    @GetMapping({"/home", "/"})
    public String home() {
        // Add any necessary data to the model
        return "home"; // "home.html" corresponds to your Thymeleaf template file
    }

    @PostMapping("/shutdown")
    public void shutdown() {
        System.exit(0);
    }
}
package com.school.torconfigtool.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

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

    /**
     * Handles GET requests to the "/setup" endpoint.
     * Returns the setup view.
     *
     * @return the name of the setup view
     */
    @GetMapping("/setup")
    public String setup() {
        // Add any necessary data to the model
        return "setup"; // "setup" corresponds to your Thymeleaf template file
    }
}
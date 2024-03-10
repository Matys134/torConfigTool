package com.school.torconfigtool.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Controller
public class SetupController {

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

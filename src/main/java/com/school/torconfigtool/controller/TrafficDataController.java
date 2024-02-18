package com.school.torconfigtool.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * This class is a Spring Boot controller responsible for handling HTTP GET requests to the "/traffic-data" endpoint.
 * It returns the name of the HTML page that displays the traffic data.
 */
@Controller
public class TrafficDataController {

    /**
     * Handles HTTP GET requests to the "/traffic-data" endpoint.
     *
     * @return The name of the HTML page that displays the traffic data.
     */
    @GetMapping("/traffic-data")
    public String getTrafficDataPage() {
        return "data"; // Return the name of your HTML page
    }
}
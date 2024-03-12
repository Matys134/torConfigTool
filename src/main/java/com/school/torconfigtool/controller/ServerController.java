package com.school.torconfigtool.controller;

import com.school.torconfigtool.service.IpAddressRetriever;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * This is a REST controller that handles operations related to server.
 * It provides an endpoint for retrieving the server IP address.
 */
@RestController
public class ServerController {

    private final IpAddressRetriever ipAddressRetriever;

    /**
     * Constructor for ServerController.
     * @param ipAddressRetriever The service for retrieving IP address.
     */
    @Autowired
    public ServerController(IpAddressRetriever ipAddressRetriever) {
        this.ipAddressRetriever = ipAddressRetriever;
    }

    /**
     * Endpoint to get the server IP address.
     * @return The server IP address.
     */
    @GetMapping("/server-ip")
    public String getServerIp() {
        return ipAddressRetriever.getLocalIpAddress();
    }
}
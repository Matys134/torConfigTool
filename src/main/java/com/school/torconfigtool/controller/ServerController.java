package com.school.torconfigtool.controller;

import com.school.torconfigtool.service.IpAddressRetriever;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ServerController {

    private final IpAddressRetriever ipAddressRetriever;

    @Autowired
    public ServerController(IpAddressRetriever ipAddressRetriever) {
        this.ipAddressRetriever = ipAddressRetriever;
    }

    @GetMapping("/server-ip")
    public String getServerIp() {
        return ipAddressRetriever.getLocalIpAddress();
    }
}
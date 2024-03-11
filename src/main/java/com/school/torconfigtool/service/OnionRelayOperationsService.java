package com.school.torconfigtool.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * OnionRelayOperationsService is a service class responsible for operations related to Onion Relays.
 */
@Service
public class OnionRelayOperationsService {

    public String readHostnameFile(String hiddenServicePort) {
        String hiddenServiceBaseDir = Paths.get(System.getProperty("user.dir"), "onion", "hiddenServiceDirs").toString();
        Path hostnameFilePath = Paths.get(hiddenServiceBaseDir, "onion-service-" + hiddenServicePort, "hostname");

        try {
            List<String> lines = Files.readAllLines(hostnameFilePath);
            return lines.isEmpty() ? "No hostname found" : lines.getFirst();
        } catch (IOException e) {
            System.err.println("Unable to read hostname file for port " + hiddenServicePort + ": " + e.getMessage());
            return "Unable to read hostname file";
        }
    }
}
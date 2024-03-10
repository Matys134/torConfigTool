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

    /**
     * This method is used to read the hostname file for a given hidden service port.
     * It constructs the path to the hostname file and then reads it.
     * If the file reading fails, it logs the error.
     *
     * @param hiddenServicePort The port of the hidden service.
     * @return The hostname if it is found, otherwise an error message.
     */
    public String readHostnameFile(String hiddenServicePort) {
        // The base directory where your hidden services directories are stored
        String hiddenServiceBaseDir = Paths.get(System.getProperty("user.dir"), "onion", "hiddenServiceDirs").toString();
        Path hostnameFilePath = Paths.get(hiddenServiceBaseDir, "onion-service-" + hiddenServicePort, "hostname");

        try {
            // Read all the lines in the hostname file and return the first line which should be the hostname
            List<String> lines = Files.readAllLines(hostnameFilePath);
            return lines.isEmpty() ? "No hostname found" : lines.getFirst();
        } catch (IOException e) {
            return "Unable to read hostname file";
        }
    }
}
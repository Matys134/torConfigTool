package com.school.torconfigtool.controllers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@RestController
@RequestMapping("/update-relay-configuration")
public class UpdateRelayConfigurationController {

    @PostMapping
    public UpdateResponse updateRelayConfiguration(@RequestBody UpdateRequest request) {
        // Build the shell command to execute the update script
        String scriptPath = "shellScripts/configure-relay.sh"; // Adjust the path to your shell script
        String command = scriptPath + " " + request.getNickname() + " " +
                request.getBandwidth() + " " + request.getPort() + " " + request.getContact();

        try {
            // Execute the shell script
            Process process = Runtime.getRuntime().exec(command);

            // Capture the script's output
            StringBuilder output = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            // Wait for the script to complete
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                // Successful update
                return new UpdateResponse(true, "Relay configuration updated successfully.");
            } else {
                // Update failed
                return new UpdateResponse(false, "Failed to update relay configuration. Script output: " + output.toString());
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return new UpdateResponse(false, "An error occurred while updating relay configuration.");
        }
    }
}

class UpdateRequest {
    private String nickname;
    private String bandwidth;
    private String port;
    private String contact;

    // Getters and setters

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getBandwidth() {
        return bandwidth;
    }

    public void setBandwidth(String bandwidth) {
        this.bandwidth = bandwidth;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }
}

class UpdateResponse {
    private boolean success;
    private String message;

    public UpdateResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    // Getters

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}

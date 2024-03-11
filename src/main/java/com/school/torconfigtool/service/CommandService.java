package com.school.torconfigtool.service;

import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class CommandService {

    /**
     * Executes a command.
     *
     * @param command The command to execute.
     * @return The process of the executed command.
     */
    public Process executeCommand(String command) {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("bash", "-c", command);
        try {
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("Command execution failed with exit code: " + exitCode);
            }
            return process;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to execute command", e);
        }
    }
}
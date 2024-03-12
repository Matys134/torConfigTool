package com.school.torconfigtool.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

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

    /**
     * This method is used to execute a bash command and get its output.
     * It returns the output as a list of strings.
     *
     * @param command The bash command to execute.
     * @return The output of the command as a list of strings.
     * @throws IOException If an I/O error occurs.
     */
    public static List<String> getCommandOutput(String command) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c", command);
        Process process = processBuilder.start();

        // Read the entire output to ensure we're not missing anything.
        List<String> outputLines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                outputLines.add(line);
            }
        }
        return outputLines;
    }
}
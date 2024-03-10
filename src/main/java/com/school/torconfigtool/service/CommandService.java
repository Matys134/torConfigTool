package com.school.torconfigtool.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    public Process executeCommand(String command) throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("bash", "-c", command);
        try {
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new Exception("Error during command execution. Exit code: " + exitCode);
            }
            return process;
        } catch (IOException | InterruptedException e) {
            throw new Exception("Error during command execution.", e);
        }
    }
}

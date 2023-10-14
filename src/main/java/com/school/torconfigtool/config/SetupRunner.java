package com.school.torconfigtool.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class SetupRunner implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        try {
            // Execute the setup script
            Process process = Runtime.getRuntime().exec("shellScripts/setup.sh");
            process.waitFor(); // Wait for the script to complete

            int exitCode = process.exitValue();
            if (exitCode == 0) {
                System.out.println("Setup script completed successfully.");
            } else {
                System.err.println("Setup script failed. Exit code: " + exitCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error executing setup script: " + e.getMessage());
        }
    }
}

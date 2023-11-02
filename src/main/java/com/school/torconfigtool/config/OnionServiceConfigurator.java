package com.school.torconfigtool.config;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class OnionServiceConfigurator {
    public static boolean configureOnionService() {
        try {
            // Check if the torrc file for the onion service exists, create it if not
            File torrcFile = new File("torrc/local-torrc-onion-service.torrc");
            if (!torrcFile.exists()) {
                torrcFile.createNewFile();
            }

            // Write the Tor Onion Service configuration to the torrc file
            FileWriter fw = new FileWriter(torrcFile);
            BufferedWriter bw = new BufferedWriter(fw);

            bw.write("HiddenServiceDir /var/lib/tor/test_website/");
            bw.newLine();
            bw.write("HiddenServicePort 80 127.0.0.1:80");

            bw.close();

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;

        }
    }

    public static boolean startOnionService() {
        try {
            // Execute a command to start the Tor Onion Service
            Process process = Runtime.getRuntime().exec("sudo systemctl start tor@onion-service");

            // Wait for the process to complete
            int exitCode = process.waitFor();

            // Check the exit code to determine if the start was successful
            return exitCode == 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}

package com.school.torconfigtool.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;

public class ProxyConfigurator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyConfigurator.class);
    private static final String TORRC_PROXY_FILE = "torrc/torrc-proxy";

    public static boolean configureProxy() {
        try {
            File torrcFile = new File(TORRC_PROXY_FILE);
            if (!torrcFile.exists() && !torrcFile.createNewFile()) {
                throw new IOException("Failed to create " + TORRC_PROXY_FILE);
            }

            // Get local IP address
            InetAddress inetAddress = InetAddress.getLocalHost();
            String localIpAddress = inetAddress.getHostAddress();

            try (BufferedWriter bw = new BufferedWriter(new FileWriter(torrcFile))) {
                bw.write("SocksPort " + localIpAddress + ":9050");
                bw.newLine();
                bw.write("SocksPolicy accept 192.168.1.0/24");
                bw.newLine();
                bw.write("RunAsDaemon 1");
                bw.newLine();
                bw.write("DNSPort " + localIpAddress + ":53");
            }
            return true;

        } catch (IOException e) {
            LOGGER.error("Failed to configure proxy", e);
            return false;
        }
    }

    public static boolean startProxy() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c", "sudo tor -f " + TORRC_PROXY_FILE);
            LOGGER.info(String.join(" ", processBuilder.command()));
            Process process = processBuilder.start();

            try {
                int exitCode = process.waitFor();
                LOGGER.info("Command exit code: {}", exitCode);
                return exitCode == 0;
            } finally {
                process.destroy();
            }

        } catch (IOException | InterruptedException e) {
            LOGGER.error("Failed to start proxy", e);
            return false;
        }
    }
}
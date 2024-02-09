package com.school.torconfigtool.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

// This class is responsible for configuring and starting a proxy.
public class ProxyConfigurator {

    // Logger for this class
    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyConfigurator.class);
    // File path for the Tor proxy configuration file
    private static final String TORRC_PROXY_FILE = "torrc/torrc-proxy";

    // Method to configure the proxy
    public static boolean configureProxy() {
        try {
            // Create a new file for the Tor proxy configuration
            File torrcFile = new File(TORRC_PROXY_FILE);
            if (!torrcFile.exists() && !torrcFile.createNewFile()) {
                throw new IOException("Failed to create " + TORRC_PROXY_FILE);
            }

            // Get local IP address
            String localIpAddress = getLocalIpAddress();

            // Write the configuration to the file
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

    // Method to start the proxy
    public static boolean startProxy() {
        try {
            // Create a new process to start the Tor proxy
            ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c", "sudo tor -f " + TORRC_PROXY_FILE);
            LOGGER.info(String.join(" ", processBuilder.command()));
            Process process = processBuilder.start();

            try {
                // Wait for the process to finish and get the exit code
                int exitCode = process.waitFor();
                LOGGER.info("Command exit code: {}", exitCode);
                return exitCode == 0;
            } finally {
                // Destroy the process
                process.destroy();
            }

        } catch (IOException | InterruptedException e) {
            LOGGER.error("Failed to start proxy", e);
            return false;
        }
    }

    // Method to get the local IP address
    private static String getLocalIpAddress() {
        try {
            // Iterate over all network interfaces
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    // Check if the address is not a loopback address and is a site local address
                    if (!inetAddress.isLoopbackAddress() && inetAddress.isSiteLocalAddress()) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            LOGGER.error("Failed to get local IP address", e);
        }
        // If no suitable address was found, return the loopback address
        return "127.0.0.1";
    }
}
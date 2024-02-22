package com.school.torconfigtool.service;

import com.school.torconfigtool.util.IpAddressRetriever;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;

import static com.school.torconfigtool.Constants.TORRC_DIRECTORY_PATH;
import static com.school.torconfigtool.Constants.TORRC_FILE_PREFIX;

/**
 * ProxyService is a service class responsible for managing a Tor Proxy.
 * It provides methods to configure, start, stop, and check the status of the proxy.
 */
@Service
public class ProxyService {

    private static final String TORRC_PROXY_FILE = TORRC_DIRECTORY_PATH + TORRC_FILE_PREFIX + "proxy";
    private final IpAddressRetriever ipAddressRetriever;
    private static final Logger logger = LoggerFactory.getLogger(ProxyService.class);

    /**
     * Constructor for ProxyService.
     *
     * @param ipAddressRetriever The IpAddressRetriever instance used to retrieve the local IP address.
     */
    public ProxyService(IpAddressRetriever ipAddressRetriever) {
        this.ipAddressRetriever = ipAddressRetriever;
    }

    /**
     * Creates a new file at the specified path.
     *
     * @param filePath The path where the file should be created.
     * @return The created File instance.
     * @throws IOException If an I/O error occurs.
     */
    public File createFile(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists() && !file.createNewFile()) {
            throw new IOException("Failed to create " + filePath);
        }
        return file;
    }

    /**
     * Writes the proxy configuration to the specified file.
     *
     * @param file           The file to write the configuration to.
     * @param localIpAddress The local IP address to use in the configuration.
     * @throws IOException If an I/O error occurs.
     */
    public void writeConfiguration(File file, String localIpAddress) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            bw.write("SocksPort " + localIpAddress + ":9050");
            bw.newLine();
            String localNetwork = localIpAddress.substring(0, localIpAddress.lastIndexOf('.')) + ".0/24";
            bw.write("SocksPolicy accept " + localNetwork);
            bw.newLine();
            bw.write("RunAsDaemon 1");
            bw.newLine();
            bw.write("DNSPort " + localIpAddress + ":53");
        }
    }

    /**
     * Configures the proxy by creating a configuration file and writing the necessary settings to it.
     *
     * @return true if the configuration was successful, false otherwise.
     * @throws IOException If an I/O error occurs.
     */
    public boolean configureProxy() throws IOException {
        File torrcFile = createFile(TORRC_PROXY_FILE);
        String localIpAddress = ipAddressRetriever.getLocalIpAddress();
        writeConfiguration(torrcFile, localIpAddress);
        return true;
    }

    /**
     * Starts the proxy.
     *
     * @return true if the proxy was started successfully, false otherwise.
     * @throws IOException      If an I/O error occurs.
     * @throws InterruptedException If the current thread is interrupted while waiting for the proxy to start.
     */
    public boolean startProxy() throws IOException, InterruptedException {
        long proxyPid = start(TORRC_PROXY_FILE);
        return proxyPid != -1;
    }

    /**
     * Stops the proxy.
     *
     * @return true if the proxy was stopped successfully, false otherwise.
     * @throws IOException      If an I/O error occurs.
     * @throws InterruptedException If the current thread is interrupted while waiting for the proxy to stop.
     */
    public boolean stopProxy() throws IOException, InterruptedException {
        return stop(TORRC_PROXY_FILE);
    }

    /**
     * Checks if the proxy is running.
     *
     * @return true if the proxy is running, false otherwise.
     * @throws IOException If an I/O error occurs.
     */
    public boolean isProxyRunning() throws IOException {
        return getRunningTorProcessId(TORRC_PROXY_FILE) != -1;
    }

    /**
     * Configures and starts the proxy.
     *
     * @return A string indicating the result of the operation.
     * @throws IOException      If an I/O error occurs.
     * @throws InterruptedException If the current thread is interrupted while waiting for the proxy to start.
     */
    public String configureAndStartProxy() throws IOException, InterruptedException {
        logger.info("Configuring Tor Proxy...");
        if (!configureProxy()) {
            logger.error("Failed to configure Tor Proxy.");
            return "Failed to configure Tor Proxy.";
        }

        logger.info("Starting Tor Proxy...");
        if (!startProxy()) {
            logger.error("Failed to start Tor Proxy.");
            return "Failed to start Tor Proxy.";
        }

        return "success";
    }

    /**
     * Starts the Tor process with the given file path.
     *
     * @param filePath The file path of the Tor configuration file.
     * @return The process ID of the started Tor process, or -1 if the process failed to start.
     * @throws IOException      If an I/O error occurs.
     * @throws InterruptedException If the current thread is interrupted while waiting for the process to start.
     */
    public long start(String filePath) throws IOException, InterruptedException {
        long pid = getRunningTorProcessId(filePath);
        if (pid != -1) {
            logger.info("Tor process already running with PID: " + pid);
            return pid;
        }

        logger.info("Attempting to start Tor process with command: sudo tor -f " + filePath);
        ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c", "sudo tor -f " + filePath);
        processBuilder.redirectErrorStream(true); // Redirect stderr to stdout
        Process process = processBuilder.start();
        try {
            logger.info("Waiting for Tor process to complete...");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    logger.info(line); // Log output of Tor process
                }
            }
            int exitCode = process.waitFor();
            logger.info("Tor process completed with exit code " + exitCode);
            if (exitCode == 0) {
                pid = process.pid();
                logger.info("Tor process started with PID: " + pid); // Log the PID immediately after the process starts
                return pid;
            }
        } finally {
            process.destroy();
        }
        return -1;
    }

    /**
     * Stops the Tor process associated with the given file path.
     *
     * @param filePath The file path of the Tor configuration file.
     * @return true if the process was successfully stopped, false otherwise.
     * @throws IOException      If an I/O error occurs.
     * @throws InterruptedException If the current thread is interrupted while waiting for the process to stop.
     */
    public boolean stop(String filePath) throws IOException, InterruptedException {
        long pid = getRunningTorProcessId(filePath);
        if (pid == -1) {
            logger.info("No running Tor process found with file path: " + filePath);
            return false;
        }

        logger.info("Attempting to stop Tor process with PID: " + pid);
        ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c", "sudo kill " + pid);
        logger.info("Command: " + processBuilder.command());
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        try {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    logger.info(line); // Log output of kill command
                }
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    logger.error(line); // Log error output of kill command
                }
            }
            int exitCode = process.waitFor();
            logger.info("Tor process stop command completed with exit code " + exitCode);
            return exitCode == 0;
        } finally {
            process.destroy();
        }
    }

    /**
     * Retrieves the process ID of the running Tor process associated with the given file path.
     *
     * @param filePath The file path of the Tor configuration file.
     * @return The process ID of the running Tor process, or -1 if no process is found.
     * @throws IOException If an I/O error occurs.
     */
    public long getRunningTorProcessId(String filePath) throws IOException {
        logger.info("Checking if Tor process is already running...");
        ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c", "ps -ef | grep tor | grep " + filePath + " | grep -v grep | awk '{print $2}'");
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        try {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line = reader.readLine();
                if (line != null) {
                    long pid = Long.parseLong(line);
                    logger.info("Tor process is already running with PID: " + pid);
                    return pid;
                }
            }
            return -1;
        } finally {
            process.destroy();
        }
    }
}
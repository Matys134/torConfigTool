package com.school.torconfigtool.service;

import org.springframework.stereotype.Service;

import java.io.*;

import static com.school.torconfigtool.util.Constants.TORRC_DIRECTORY_PATH;
import static com.school.torconfigtool.util.Constants.TORRC_FILE_PREFIX;

/**
 * ProxyService is a service class responsible for managing a Tor Proxy.
 * It provides methods to configure, start, stop, and check the status of the proxy.
 */
@Service
public class ProxyService {

    private static final String TORRC_PROXY_FILE = TORRC_DIRECTORY_PATH + TORRC_FILE_PREFIX + "proxy";
    private final IpAddressRetriever ipAddressRetriever;
    private final RelayStatusService relayStatusService;

    /**
     * Constructor for ProxyService.
     *
     * @param ipAddressRetriever The IpAddressRetriever instance used to retrieve the local IP address.
     */
    public ProxyService(IpAddressRetriever ipAddressRetriever, RelayStatusService relayStatusService) {
        this.ipAddressRetriever = ipAddressRetriever;
        this.relayStatusService = relayStatusService;
    }

    /**
     * Creates a new file at the specified path.
     *
     * @param filePath The path where the file should be created.
     * @return The created File instance.
     * @throws IOException If an I/O error occurs.
     */
    public File createProxyFile(String filePath) throws IOException {
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
    public void writeConfiguration(File file, String localIpAddress, int socksPort) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            bw.write("SocksPort " + localIpAddress + ":" + socksPort);
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
    public boolean configureProxy(int socksPort) throws IOException {
        File torrcFile = createProxyFile(TORRC_PROXY_FILE);
        String localIpAddress = ipAddressRetriever.getLocalIpAddress();
        writeConfiguration(torrcFile, localIpAddress, socksPort);
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
        long proxyPid = startProxy(TORRC_PROXY_FILE);
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
        return stopProxy(TORRC_PROXY_FILE);
    }

    /**
     * Checks if the proxy is running.
     *
     * @return true if the proxy is running, false otherwise.
     * @throws IOException If an I/O error occurs.
     */
    public boolean isProxyRunning() throws IOException {
        return relayStatusService.getTorRelayPID(TORRC_PROXY_FILE) != -1;
    }

    /**
     * Configures and starts the proxy.
     *
     * @return A string indicating the result of the operation.
     * @throws IOException      If an I/O error occurs.
     * @throws InterruptedException If the current thread is interrupted while waiting for the proxy to start.
     */
    public String configureAndStartProxy(int socksPort) throws IOException, InterruptedException {
        if (!configureProxy(socksPort)) {
            return "Failed to configure Tor Proxy.";
        }

        if (!startProxy()) {
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
    public long startProxy(String filePath) throws IOException, InterruptedException {
        long pid = relayStatusService.getTorRelayPID(filePath);
        if (pid != -1) {
            return pid;
        }

        ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c", "sudo tor -f " + filePath);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        try {
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                pid = process.pid();
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
    public boolean stopProxy(String filePath) throws IOException, InterruptedException {
        long pid = relayStatusService.getTorRelayPID(filePath);
        if (pid == -1) {
            return false;
        }

        ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c", "sudo kill " + pid);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        try {
            int exitCode = process.waitFor();
            return exitCode == 0;
        } finally {
            process.destroy();
        }
    }

    public int getSocksPort() throws IOException {
        File torrcFile = new File(TORRC_PROXY_FILE);
        try (BufferedReader br = new BufferedReader(new FileReader(torrcFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("SocksPort")) {
                    String[] parts = line.split(" ");
                    String[] ipAndPort = parts[1].split(":");
                    return Integer.parseInt(ipAndPort[1]);
                }
            }
        }
        throw new IOException("Failed to retrieve SOCKS port from " + TORRC_PROXY_FILE);
    }
}
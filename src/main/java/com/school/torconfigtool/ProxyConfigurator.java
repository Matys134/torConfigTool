package com.school.torconfigtool;

import com.school.torconfigtool.util.IpAddressRetriever;

import java.io.File;
import java.io.IOException;

/**
 * This class is responsible for configuring, starting, stopping, and checking the status of a proxy.
 */
public class ProxyConfigurator {

    private static final String TORRC_PROXY_FILE = "torrc/torrc-proxy";

    private final ProxyFileCreator proxyFileCreator;
    private final ProxyStarter proxyStarter;
    private final IpAddressRetriever ipAddressRetriever;

    /**
     * Constructor for the ProxyConfigurator class.
     *
     * @param proxyFileCreator   The object responsible for creating the proxy file.
     * @param proxyStarter       The object responsible for starting the proxy.
     * @param ipAddressRetriever The object responsible for retrieving the IP address.
     */
    public ProxyConfigurator(ProxyFileCreator proxyFileCreator, ProxyStarter proxyStarter, IpAddressRetriever ipAddressRetriever) {
        this.proxyFileCreator = proxyFileCreator;
        this.proxyStarter = proxyStarter;
        this.ipAddressRetriever = ipAddressRetriever;
    }

    /**
     * Configures the proxy by creating a file and writing the configuration to it.
     *
     * @return true if the configuration is successful.
     * @throws IOException if an I/O error occurs.
     */
    public boolean configureProxy() throws IOException {
        File torrcFile = proxyFileCreator.createFile(TORRC_PROXY_FILE);
        String localIpAddress = ipAddressRetriever.getLocalIpAddress();
        proxyFileCreator.writeConfiguration(torrcFile, localIpAddress);
        return true;
    }

    /**
     * Starts the proxy.
     *
     * @return true if the proxy is successfully started.
     * @throws IOException, InterruptedException if an error occurs during the process.
     */
    public boolean startProxy() throws IOException, InterruptedException {
        long proxyPid = proxyStarter.start(TORRC_PROXY_FILE);
        return proxyPid != -1;
    }

    /**
     * Stops the proxy.
     *
     * @return true if the proxy is successfully stopped.
     * @throws IOException, InterruptedException if an error occurs during the process.
     */
    public boolean stopProxy() throws IOException, InterruptedException {
        return proxyStarter.stop(TORRC_PROXY_FILE);
    }

    /**
     * Checks if the proxy is running.
     *
     * @return true if the proxy is running.
     * @throws IOException if an I/O error occurs.
     */
    public boolean isProxyRunning() throws IOException {
        return proxyStarter.getRunningTorProcessId(TORRC_PROXY_FILE) != -1;
    }
}
package com.school.torconfigtool.proxy.config;

import com.school.torconfigtool.proxy.util.ProxyStarter;
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
     * @param proxyFileCreator  An instance of ProxyFileCreator to create proxy configuration files.
     * @param proxyStarter      An instance of ProxyStarter to start and stop the proxy.
     * @param ipAddressRetriever An instance of IpAddressRetriever to retrieve the local IP address.
     */
    public ProxyConfigurator(ProxyFileCreator proxyFileCreator, ProxyStarter proxyStarter, IpAddressRetriever ipAddressRetriever) {
        this.proxyFileCreator = proxyFileCreator;
        this.proxyStarter = proxyStarter;
        this.ipAddressRetriever = ipAddressRetriever;
    }

    /**
     * Configures the proxy by creating a configuration file and writing the local IP address to it.
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
     * @throws IOException if an I/O error occurs.
     * @throws InterruptedException if the current thread is interrupted while waiting for the proxy to start.
     */
    public boolean startProxy() throws IOException, InterruptedException {
        long proxyPid = proxyStarter.start(TORRC_PROXY_FILE);
        return proxyPid != -1;
    }

    /**
     * Stops the proxy.
     *
     * @return true if the proxy is successfully stopped.
     * @throws IOException if an I/O error occurs.
     * @throws InterruptedException if the current thread is interrupted while waiting for the proxy to stop.
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
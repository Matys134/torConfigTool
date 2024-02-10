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

public class ProxyConfigurator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyConfigurator.class);
    private static final String TORRC_PROXY_FILE = "torrc/torrc-proxy";

    private final ProxyFileCreator proxyFileCreator;
    private final ProxyStarter proxyStarter;
    private final IpAddressRetriever ipAddressRetriever;

    private long proxyPid = -1;

    public ProxyConfigurator(ProxyFileCreator proxyFileCreator, ProxyStarter proxyStarter, IpAddressRetriever ipAddressRetriever) {
        this.proxyFileCreator = proxyFileCreator;
        this.proxyStarter = proxyStarter;
        this.ipAddressRetriever = ipAddressRetriever;
    }

    public boolean configureProxy() throws IOException {
        File torrcFile = proxyFileCreator.createFile(TORRC_PROXY_FILE);
        String localIpAddress = ipAddressRetriever.getLocalIpAddress();
        proxyFileCreator.writeConfiguration(torrcFile, localIpAddress);
        return true;
    }

    public boolean startProxy() throws IOException, InterruptedException {
        proxyPid = proxyStarter.start(TORRC_PROXY_FILE);
        return proxyPid != -1;
    }

    public boolean stopProxy() throws IOException, InterruptedException {
        if (proxyPid != -1) {
            return proxyStarter.stop(proxyPid);
        }
        return false;
    }

    public boolean isProxyRunning() throws IOException {
        return proxyStarter.getRunningTorProcessId() != -1;
    }
}
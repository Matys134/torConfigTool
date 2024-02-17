package com.school.torconfigtool;

import com.school.torconfigtool.util.IpAddressRetriever;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

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
        return proxyStarter.stop(TORRC_PROXY_FILE);
    }

    public boolean isProxyRunning() throws IOException {
        return proxyStarter.getRunningTorProcessId(TORRC_PROXY_FILE) != -1;
    }
}
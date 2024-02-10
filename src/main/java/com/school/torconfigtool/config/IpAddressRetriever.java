package com.school.torconfigtool.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Optional;

public class IpAddressRetriever {

    private static final Logger LOGGER = LoggerFactory.getLogger(IpAddressRetriever.class);

    public Optional<String> getLocalIpAddress() {
        return getFirstNonLoopbackAddress();
    }

    private Optional<String> getFirstNonLoopbackAddress() {
        try {
            return Collections.list(NetworkInterface.getNetworkInterfaces()).stream()
                    .flatMap(networkInterface -> getFirstNonLoopbackAddressFromInterface(networkInterface).stream())
                    .findFirst();
        } catch (SocketException e) {
            LOGGER.error("Failed to get local IP address", e);
            return Optional.empty();
        }
    }

    private Optional<String> getFirstNonLoopbackAddressFromInterface(NetworkInterface networkInterface) {
        return Collections.list(networkInterface.getInetAddresses()).stream()
                .filter(this::isNonLoopbackSiteLocalAddress)
                .map(InetAddress::getHostAddress)
                .findFirst();
    }

    private boolean isNonLoopbackSiteLocalAddress(InetAddress inetAddress) {
        return !inetAddress.isLoopbackAddress() && inetAddress.isSiteLocalAddress();
    }
}
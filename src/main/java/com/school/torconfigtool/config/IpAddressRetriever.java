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
        try {
            return getFirstNonLoopbackAddress();
        } catch (SocketException e) {
            LOGGER.error("Failed to get local IP address", e);
            return Optional.empty();
        }
    }

    private Optional<String> getFirstNonLoopbackAddress() throws SocketException {
        return Collections.list(NetworkInterface.getNetworkInterfaces()).stream()
                .flatMap(networkInterface -> getFirstNonLoopbackAddressFromInterface(networkInterface).stream())
                .findFirst();
    }

    private Optional<String> getFirstNonLoopbackAddressFromInterface(NetworkInterface networkInterface) {
        return Collections.list(networkInterface.getInetAddresses()).stream()
                .filter(inetAddress -> !inetAddress.isLoopbackAddress() && inetAddress.isSiteLocalAddress())
                .map(InetAddress::getHostAddress)
                .findFirst();
    }
}
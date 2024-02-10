package com.school.torconfigtool.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
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
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();
            Optional<String> address = getFirstNonLoopbackAddressFromInterface(networkInterface);
            if (address.isPresent()) {
                return address;
            }
        }
        return Optional.empty();
    }

    private Optional<String> getFirstNonLoopbackAddressFromInterface(NetworkInterface networkInterface) {
        Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
        while (inetAddresses.hasMoreElements()) {
            InetAddress inetAddress = inetAddresses.nextElement();
            if (!inetAddress.isLoopbackAddress() && inetAddress.isSiteLocalAddress()) {
                return Optional.of(inetAddress.getHostAddress());
            }
        }
        return Optional.empty();
    }
}
package com.school.torconfigtool.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Optional;

/**
 * This class is used to retrieve the local IP address of the machine where the application is running.
 */
public class IpAddressRetriever {

    // Logger instance for logging events
    private static final Logger LOGGER = LoggerFactory.getLogger(IpAddressRetriever.class);

    /**
     * This method is used to get the local IP address.
     * @return An Optional<String> containing the local IP address if found, otherwise an empty Optional.
     */
    public Optional<String> getLocalIpAddress() {
        return getFirstNonLoopbackAddress();
    }

    /**
     * This method is used to get the first non-loopback address from the network interfaces.
     * @return An Optional<String> containing the first non-loopback address if found, otherwise an empty Optional.
     */
    private Optional<String> getFirstNonLoopbackAddress() {
        try {
            return Collections.list(NetworkInterface.getNetworkInterfaces()).stream()
                    .map(this::getFirstNonLoopbackAddressFromInterface)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .findFirst();
        } catch (SocketException e) {
            LOGGER.error("Failed to get local IP address", e);
            return Optional.empty();
        }
    }

    /**
     * This method is used to get the first non-loopback address from a specific network interface.
     * @param networkInterface The network interface to check for non-loopback addresses.
     * @return An Optional<String> containing the first non-loopback address if found, otherwise an empty Optional.
     */
    private Optional<String> getFirstNonLoopbackAddressFromInterface(NetworkInterface networkInterface) {
        return Collections.list(networkInterface.getInetAddresses()).stream()
                .filter(this::isNonLoopbackSiteLocalAddress)
                .map(InetAddress::getHostAddress)
                .findFirst();
    }

    /**
     * This method is used to check if an InetAddress is a non-loopback and site local address.
     * @param inetAddress The InetAddress to check.
     * @return A boolean indicating whether the InetAddress is a non-loopback and site local address.
     */
    private boolean isNonLoopbackSiteLocalAddress(InetAddress inetAddress) {
        return !inetAddress.isLoopbackAddress() && inetAddress.isSiteLocalAddress();
    }
}
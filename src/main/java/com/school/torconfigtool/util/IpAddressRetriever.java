package com.school.torconfigtool.util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * This class is responsible for retrieving the local IP address of the machine where the application is running.
 */
public class IpAddressRetriever {

    // Logger instance for this class
    private static final Logger LOGGER = LoggerFactory.getLogger(IpAddressRetriever.class);

    /**
     * This method retrieves the local IP address of the machine.
     * It iterates over all the network interfaces of the machine and returns the first non-loopback, site local address it finds.
     * If no such address is found, it defaults to "127.0.0.1".
     * If there is a SocketException while retrieving the network interfaces, it logs the error and returns "127.0.0.1".
     *
     * @return A string representing the local IP address.
     */
    public String getLocalIpAddress() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            return processNetworkInterfaces(networkInterfaces);
        } catch (SocketException e) {
            LOGGER.error("Failed to get local IP address", e);
        }
        return "127.0.0.1";
    }

    /**
     * This method processes the given Enumeration of NetworkInterfaces.
     * It iterates over all the network interfaces and their associated InetAddress objects.
     * It returns the first non-loopback, site local address it finds.
     * If no such address is found, it defaults to "127.0.0.1".
     *
     * @param networkInterfaces An Enumeration of NetworkInterface objects to process.
     * @return A string representing the local IP address.
     */
    private String processNetworkInterfaces(Enumeration<NetworkInterface> networkInterfaces) {
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();
            Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
            while (inetAddresses.hasMoreElements()) {
                InetAddress inetAddress = inetAddresses.nextElement();
                if (!inetAddress.isLoopbackAddress() && inetAddress.isSiteLocalAddress()) {
                    return inetAddress.getHostAddress();
                }
            }
        }
        return "127.0.0.1";
    }
}
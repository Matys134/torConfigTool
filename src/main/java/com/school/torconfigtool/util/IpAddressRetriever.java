package com.school.torconfigtool.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * This class is used to retrieve the local IP address of the machine where the application is running.
 */

@Service
public class IpAddressRetriever {

    // Logger instance for this class
    private static final Logger LOGGER = LoggerFactory.getLogger(IpAddressRetriever.class);

    /**
     * This method is used to get the local IP address of the machine.
     * It iterates over all the network interfaces and their associated IP addresses.
     * It returns the first non-loopback, site local IP address it finds.
     * If no such address is found or an error occurs, it returns the loopback address "127.0.0.1".
     *
     * @return A string representing the local IP address.
     */
    public String getLocalIpAddress() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
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
        } catch (SocketException e) {
            // Log the error if we fail to get the local IP address
            LOGGER.error("Failed to get local IP address", e);
        }
        // Return the loopback address if no local IP address was found or an error occurred
        return "127.0.0.1";
    }
}
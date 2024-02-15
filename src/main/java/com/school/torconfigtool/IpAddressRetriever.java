package com.school.torconfigtool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class IpAddressRetriever {

    private static final Logger LOGGER = LoggerFactory.getLogger(IpAddressRetriever.class);

    public String getLocalIpAddress() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            return processNetworkInterfaces(networkInterfaces);
        } catch (SocketException e) {
            LOGGER.error("Failed to get local IP address", e);
        }
        return "127.0.0.1";
    }

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
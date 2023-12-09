package com.school.torconfigtool.service;

import com.school.torconfigtool.models.BaseRelayConfig;
import com.school.torconfigtool.models.BridgeRelayConfig;
import com.school.torconfigtool.models.GuardRelayConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class TorrcFileCreator {

    private static final Logger logger = LoggerFactory.getLogger(TorrcFileCreator.class);

    public static void createTorrcFile(String filePath, BaseRelayConfig config) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("Nickname " + config.getNickname());
            writer.newLine();
            writer.write("ORPort " + config.getOrPort());
            writer.newLine();
            writer.write("ContactInfo " + config.getContact());
            writer.newLine();
            writer.write("ControlPort " + config.getControlPort());
            writer.newLine();
            writer.write("SocksPort 0");
            writer.newLine();
            writer.write("RunAsDaemon 1");
            writer.newLine();

            if (config instanceof BridgeRelayConfig) {
                config.writeSpecificConfig(writer);
            } else if (config instanceof GuardRelayConfig) {
                config.writeSpecificConfig(writer);
            } else {
                logger.error("Unknown relay type");
            }

            String systemIpv6 = getSystemIpv6();

            if (systemIpv6 != null && InetAddress.getByName("::0").isReachable(2000)) {
                writer.newLine();
                writer.write("ORPort " + systemIpv6 + ":" + config.getOrPort());
                writer.newLine();
            }

            // Add any other common configurations from BaseRelayConfig
            String currentDirectory = System.getProperty("user.dir");
            String dataDirectoryPath = currentDirectory + File.separator + "torrc" + File.separator + "dataDirectory" + File.separator + config.getNickname();
            writer.write("DataDirectory " + dataDirectoryPath);

            // Use the new method to write specific configurations
            config.writeSpecificConfig(writer);

        } catch (IOException e) {
            logger.error("Error creating Torrc file", e);
        }
    }

    static String getSystemIpv6() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.isLoopback() || !iface.isUp())
                    continue;

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while(addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet6Address) {
                        if(!addr.isLinkLocalAddress()){
                            String hostAddress = addr.getHostAddress();
                            int idx = hostAddress.indexOf('%');
                            return (idx > 0) ? hostAddress.substring(0, idx) : hostAddress;
                        }
                    }
                }
            }
        } catch (SocketException e) {
            logger.error("Socket exception when getting IPv6 address", e);
        }
        return null;
    }
}

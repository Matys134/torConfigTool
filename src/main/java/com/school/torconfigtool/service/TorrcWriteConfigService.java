package com.school.torconfigtool.service;

import com.school.torconfigtool.model.BaseRelayConfig;
import com.school.torconfigtool.model.BridgeConfig;
import com.school.torconfigtool.model.GuardConfig;
import com.school.torconfigtool.model.RelayConfig;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;

import static com.school.torconfigtool.util.Constants.TORRC_DIRECTORY_PATH;

@Service
public class TorrcWriteConfigService {

    private final SnowflakeProxyService snowflakeProxyService = new SnowflakeProxyService();

    public void writeConfig(RelayConfig config, BufferedWriter writer) throws IOException {
        if (config instanceof BridgeConfig) {
            writeBridgeConfig((BridgeConfig) config, writer);
        } else if (config instanceof GuardConfig) {
            writeGuardConfig();
        } else {
            throw new IllegalArgumentException("Unknown relay type");
        }
    }

    private void writeBridgeConfig(BridgeConfig config, BufferedWriter writer) throws IOException {
        writer.write("BridgeRelay 1");
        writer.newLine();
        switch (config.getBridgeType()) {
            case "obfs4":
                writer.write("ServerTransportPlugin obfs4 exec /usr/bin/obfs4proxy");
                writer.newLine();
                writer.write("ServerTransportListenAddr obfs4 0.0.0.0:" + config.getServerTransport());
                writer.newLine();
                writer.write("ExtORPort auto");
                writer.newLine();
                break;
            case "webtunnel":
                writer.write("ServerTransportPlugin webtunnel exec /usr/local/bin/webtunnel");
                writer.newLine();
                writer.write("ServerTransportListenAddr webtunnel 127.0.0.1:15000");
                writer.newLine();
                writer.write("ServerTransportOptions webtunnel url=https://" + config.getWebtunnelUrl() + "/" + config.getPath());
                writer.newLine();
                writer.write("ExtORPort auto");
                writer.newLine();
                break;
            case "snowflake":
                snowflakeProxyService.setupSnowflakeProxy();
                break;
            default:
                throw new IllegalArgumentException("Unknown bridge type");
        }
    }

    private void writeGuardConfig() {

    }

    public void writeOnionServiceConfig(int onionServicePort, BufferedWriter torrcWriter) throws IOException {
        String currentDirectory = System.getProperty("user.dir");
        String hiddenServiceDirs = currentDirectory + "/onion/hiddenServiceDirs";

        torrcWriter.write("HiddenServiceDir " + hiddenServiceDirs + "/onion-service-" + onionServicePort + "/");
        torrcWriter.newLine();
        torrcWriter.write("HiddenServicePort 80 127.0.0.1:" + onionServicePort);
        torrcWriter.newLine();
        torrcWriter.write("RunAsDaemon 1");
        torrcWriter.newLine();
        torrcWriter.write("SocksPort 0");
        torrcWriter.newLine();
        // Write the DataDirectory configuration to the file
        String dataDirectoryPath = currentDirectory + "/" + TORRC_DIRECTORY_PATH + "dataDirectory/onion_" + onionServicePort;
        torrcWriter.write("DataDirectory " + dataDirectoryPath);
        torrcWriter.newLine();
    }

    public void writeTorrcFileConfig(BaseRelayConfig config, BufferedWriter writer) throws IOException {
        // Write the nickname to the file
        writer.write("Nickname " + config.getNickname());
        writer.newLine();

        // Write the ORPort to the file, defaulting to "127.0.0.1:auto" if not provided
        String orPort = config.getOrPort() != null ? config.getOrPort() : "127.0.0.1:auto";
        writer.write("ORPort " + orPort + " IPv4Only");
        writer.newLine();

        // Write the contact info to the file
        writer.write("ContactInfo " + config.getContact());
        writer.newLine();

        // Write the control port to the file
        writer.write("ControlPort " + config.getControlPort());
        writer.newLine();

        writer.write("CookieAuthentication 1");
        writer.newLine();

        // Write the SocksPort to the file
        writer.write("SocksPort 0");
        writer.newLine();

        // Write the RunAsDaemon configuration to the file
        writer.write("RunAsDaemon 1");
        writer.newLine();

        // Write the DataDirectory configuration to the file
        String relayType = config.getClass().getSimpleName(); // Get the class name as the relay type
        String dataDirectoryPath = System.getProperty("user.dir") + File.separator + "torrc" + File.separator + "dataDirectory" + File.separator + config.getNickname() + "_" + relayType;
        writer.write("DataDirectory " + dataDirectoryPath);
        writer.newLine();

        // Write the BandwidthRate to the file if provided
        if (config.getBandwidthRate() != null) {
            writer.write("RelayBandwidthRate " + config.getBandwidthRate() + " KBytes");
            writer.newLine();
        }

        // Write specific configuration based on the type of relay
        writeConfig(config, writer);
    }
}
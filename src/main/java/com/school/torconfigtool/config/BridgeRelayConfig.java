package com.school.torconfigtool.config;

import com.school.torconfigtool.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents the configuration for a Bridge Relay in the Tor network.
 * It extends the BaseRelayConfig class and provides additional properties and methods specific to a Bridge Relay.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BridgeRelayConfig extends BaseRelayConfig {
    private String serverTransport;
    private String relayType;
    private String webtunnelDomain;
    private int webtunnelPort;
    private String webtunnelUrl;
    private String email;
    private String path;
    private String bridgeType;
    private static final Logger logger = LoggerFactory.getLogger(BridgeRelayConfig.class);
    private static final String TORRC_DIRECTORY_PATH = "torrc/";

    private SnowflakeProxyRunner snowflakeProxyRunner = new SnowflakeProxyRunner();

    private Map<String, BridgeConfigWriter> configWriters;

    /**
     * Constructor for the BridgeRelayConfig class.
     * Initializes the configWriters map with instances of Obfs4ConfigWriter, WebtunnelConfigWriter, and SnowflakeConfigWriter.
     */
    public BridgeRelayConfig() {
        configWriters = new HashMap<>();
        configWriters.put("obfs4", new Obfs4ConfigWriter(this));
        configWriters.put("webtunnel", new WebtunnelConfigWriter(this));
        configWriters.put("snowflake", new SnowflakeConfigWriter(this));
    }

    /**
     * Writes the specific configuration for the Bridge Relay to the provided BufferedWriter.
     * @param writer BufferedWriter to write the configuration to.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public void writeSpecificConfig(BufferedWriter writer) throws IOException {
        writer.write("BridgeRelay 1");
        writer.newLine();
        logger.info("Writing specific config for bridge type: " + getBridgeType());
        BridgeConfigWriter configWriter = configWriters.get(getBridgeType());
        if (configWriter != null) {
            configWriter.writeConfig(writer);
        } else {
            logger.error("Unknown bridge type: " + getBridgeType());
        }
    }

    /**
     * Writes the configuration for the Bridge Relay to the provided BufferedWriter.
     * @param writer BufferedWriter to write the configuration to.
     * @param bridgeType The type of bridge.
     * @param execCommand The command to execute.
     * @param listenAddress The address to listen on.
     * @param additionalOptions Additional options for the configuration.
     * @throws IOException If an I/O error occurs.
     */
    public void writeConfig(BufferedWriter writer, String bridgeType, String execCommand, String listenAddress, String additionalOptions) throws IOException {
        writer.write("ServerTransportPlugin " + bridgeType + " exec " + execCommand);
        writer.newLine();
        writer.write("ServerTransportListenAddr " + bridgeType + " " + listenAddress);
        writer.newLine();
        writer.write("ExtORPort auto");
        writer.newLine();
        if (additionalOptions != null) {
            writer.write(additionalOptions);
            writer.newLine();
        }
    }

    /**
     * Sets the bridge type for this BridgeRelayConfig.
     * @param bridgeType The type of bridge.
     */
    public void setBridgeType(String bridgeType) {
        this.bridgeType = bridgeType;
        logger.info("Bridge type set in BridgeRelayConfig: " + this.bridgeType);
    }

    /**
     * Sets the webtunnel URL for this BridgeRelayConfig.
     * @param webtunnelUrl The webtunnel URL.
     */
    public void setWebtunnelUrl(String webtunnelUrl) {
        this.webtunnelUrl = webtunnelUrl;
        logger.info("Webtunnel URL set in BridgeRelayConfig: " + this.webtunnelUrl);
    }
}
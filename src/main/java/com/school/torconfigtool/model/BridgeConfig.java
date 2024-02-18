package com.school.torconfigtool.model;

import com.school.torconfigtool.service.SnowflakeProxyService;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;

/**
 * The BridgeConfig class extends the BaseRelayConfig class.
 * It represents the configuration for a Bridge Relay in the Tor network.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BridgeConfig extends BaseRelayConfig {
    // The server transport of the bridge
    private String serverTransport;
    // The type of the relay
    private String relayType;
    // The domain of the webtunnel
    private String webtunnelDomain;
    // The port of the webtunnel
    private int webtunnelPort;
    // The URL of the webtunnel
    private String webtunnelUrl;
    // The email of the contact
    private String email;
    // The path of the webtunnel
    private String path;
    // The type of the bridge
    private String bridgeType;
    // Logger for this class
    private static final Logger logger = LoggerFactory.getLogger(BridgeConfig.class);
    // The path to the directory where the torrc files are stored
    private static final String TORRC_DIRECTORY_PATH = "torrc/";
    // The SnowflakeProxyService instance
    private SnowflakeProxyService snowflakeProxyService = new SnowflakeProxyService();

    /**
     * Writes the specific configuration for a Bridge Relay.
     * This method is overridden from the BaseRelayConfig class to provide specific configuration for a Bridge Relay.
     *
     * @param writer the BufferedWriter to write the configuration to
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void writeSpecificConfig(BufferedWriter writer) throws IOException {
        writer.write("BridgeRelay 1");
        writer.newLine();
        logger.info("Writing specific config for bridge type: " + getBridgeType());
        switch (getBridgeType()) {
            case "obfs4":
                writer.write("ServerTransportPlugin obfs4 exec /usr/bin/obfs4proxy");
                writer.newLine();
                writer.write("ServerTransportListenAddr obfs4 0.0.0.0:" + getServerTransport());
                writer.newLine();
                writer.write("ExtORPort auto");
                writer.newLine();
                writer.write("ContactInfo " + getContact());
                writer.newLine();
                break;
            case "webtunnel":
                writer.write("ServerTransportPlugin webtunnel exec /usr/local/bin/webtunnel");
                writer.newLine();
                writer.write("ServerTransportListenAddr webtunnel 127.0.0.1:15000");
                writer.newLine();
                writer.write("ServerTransportOptions webtunnel url=https://" + getWebtunnelUrl() + "/" + getPath());
                writer.newLine();
                writer.write("ExtORPort auto");
                writer.newLine();
                break;
            case "snowflake":
                snowflakeProxyService.runSnowflakeProxy();
                break;
            default:
                logger.error("Unknown bridge type: " + getBridgeType());
        }
    }

    /**
     * Sets the bridge type.
     *
     * @param bridgeType the type of the bridge
     */
    public void setBridgeType(String bridgeType) {
        this.bridgeType = bridgeType;
        logger.info("Bridge type set in BridgeRelayConfig: " + this.bridgeType);
    }

    /**
     * Sets the webtunnel URL.
     *
     * @param webtunnelUrl the URL of the webtunnel
     */
    public void setWebtunnelUrl(String webtunnelUrl) {
        this.webtunnelUrl = webtunnelUrl;
        logger.info("Webtunnel URL set in BridgeRelayConfig: " + this.webtunnelUrl);
    }
}
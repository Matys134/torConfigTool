package com.school.torconfigtool.model;

import com.school.torconfigtool.service.SnowflakeProxyService;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    // The SnowflakeProxyService instance
    private SnowflakeProxyService snowflakeProxyService = new SnowflakeProxyService();

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
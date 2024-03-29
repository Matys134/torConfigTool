package com.school.torconfigtool.model;

import com.school.torconfigtool.service.SnowflakeProxyService;
import lombok.Data;
import lombok.EqualsAndHashCode;

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
    // The obfs4 link of the bridge
    private String obfs4Link;
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
    // The SnowflakeProxyService instance
    private SnowflakeProxyService snowflakeProxyService = new SnowflakeProxyService();

}
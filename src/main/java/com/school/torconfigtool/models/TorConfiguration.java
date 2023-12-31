package com.school.torconfigtool.models;

import lombok.Data;

@Data
public class TorConfiguration {
    private GuardRelayConfig guardRelayConfig;
    private BridgeRelayConfig bridgeRelayConfig;
    private String hiddenServiceDir;
    private String hiddenServicePort;
    private String bandwidthRate;
    private String hostname;
    // Other Tor-specific configurations...
    // Add methods if needed...
}
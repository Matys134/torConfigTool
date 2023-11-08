package com.school.torconfigtool.models;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BridgeRelayConfig extends BaseRelayConfig {
    private String bridgeTransportListenAddr;
    private String relayType;
    // Add bridge-specific attributes and methods here...
}
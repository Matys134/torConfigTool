package com.school.torconfigtool.models;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BridgeRelayConfig extends GuardRelayConfig {
    private String bridgeTransportListenAddr;
    private String relayType;
}

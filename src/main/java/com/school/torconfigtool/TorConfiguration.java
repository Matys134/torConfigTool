package com.school.torconfigtool;

import com.school.torconfigtool.bridge.BridgeConfig;
import lombok.Data;

@Data
public class TorConfiguration {
    private GuardRelayConfig guardRelayConfig;
    private BridgeConfig bridgeConfig;
    private String hiddenServiceDir;
    private String hiddenServicePort;
    private String bandwidthRate;
    private String hostname;
}
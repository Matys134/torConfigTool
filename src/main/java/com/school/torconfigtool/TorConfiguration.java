package com.school.torconfigtool;

import com.school.torconfigtool.bridge.config.BridgeRelayConfig;
import com.school.torconfigtool.guard.config.GuardRelayConfig;
import lombok.Data;

@Data
public class TorConfiguration {
    private GuardRelayConfig guardRelayConfig;
    private BridgeRelayConfig bridgeRelayConfig;
    private String hiddenServiceDir;
    private String hiddenServicePort;
    private String bandwidthRate;
    private String hostname;
}
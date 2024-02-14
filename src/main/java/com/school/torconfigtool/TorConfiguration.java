package com.school.torconfigtool;

import com.school.torconfigtool.config.BridgeRelayConfig;
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
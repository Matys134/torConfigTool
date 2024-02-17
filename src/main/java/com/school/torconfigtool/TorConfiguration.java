package com.school.torconfigtool;

import com.school.torconfigtool.bridge.BridgeConfig;
import com.school.torconfigtool.guard.GuardConfig;
import lombok.Data;

@Data
public class TorConfiguration {
    private GuardConfig guardConfig;
    private BridgeConfig bridgeConfig;
    private String hiddenServiceDir;
    private String hiddenServicePort;
    private String bandwidthRate;
    private String hostname;
}
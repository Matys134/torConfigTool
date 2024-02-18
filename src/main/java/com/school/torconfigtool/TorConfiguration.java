package com.school.torconfigtool;

import com.school.torconfigtool.model.BridgeConfig;
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
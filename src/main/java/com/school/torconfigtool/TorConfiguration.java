package com.school.torconfigtool;

import com.school.torconfigtool.model.BridgeConfig;
import com.school.torconfigtool.model.GuardConfig;
import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class TorConfiguration {
    private GuardConfig guardConfig;
    private BridgeConfig bridgeConfig;
    private String hiddenServiceDir;
    private String hiddenServicePort;
    private String bandwidthRate;
    private String hostname;
}
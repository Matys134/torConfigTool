package com.school.torconfigtool;

import java.io.BufferedWriter;

public class SnowflakeConfigWriter implements BridgeConfigWriter {
    private BridgeRelayConfig config;

    public SnowflakeConfigWriter(BridgeRelayConfig config) {
        this.config = config;
    }

    @Override
    public void writeConfig(BufferedWriter writer) {
        config.writeSnowflakeConfig();
    }
}

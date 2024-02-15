package com.school.torconfigtool;

import com.school.torconfigtool.bridge.BridgeConfigWriter;
import com.school.torconfigtool.bridge.config.BridgeRelayConfig;

import java.io.BufferedWriter;

public class SnowflakeConfigWriter implements BridgeConfigWriter {
    private BridgeRelayConfig config;
    private SnowflakeProxyRunner snowflakeProxyRunner = new SnowflakeProxyRunner();

    public SnowflakeConfigWriter(BridgeRelayConfig config) {
        this.config = config;
    }

    @Override
    public void writeConfig(BufferedWriter writer) {
        writeSnowflakeConfig();
    }

    private void writeSnowflakeConfig() {
        snowflakeProxyRunner.runSnowflakeProxy();
    }
}
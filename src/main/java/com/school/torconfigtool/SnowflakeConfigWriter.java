package com.school.torconfigtool;

import java.io.BufferedWriter;

public class SnowflakeConfigWriter implements BridgeConfigWriter {
    private SnowflakeProxyRunner snowflakeProxyRunner;

    public SnowflakeConfigWriter(SnowflakeProxyRunner snowflakeProxyRunner) {
        this.snowflakeProxyRunner = snowflakeProxyRunner;
    }

    @Override
    public void writeConfig(BufferedWriter writer) {
        snowflakeProxyRunner.runSnowflakeProxy();
    }
}
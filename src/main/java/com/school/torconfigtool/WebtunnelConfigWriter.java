package com.school.torconfigtool;

import com.school.torconfigtool.bridge.BridgeConfigWriter;
import com.school.torconfigtool.bridge.config.BridgeRelayConfig;

import java.io.BufferedWriter;
import java.io.IOException;

public class WebtunnelConfigWriter implements BridgeConfigWriter {
    private BridgeRelayConfig config;

    public WebtunnelConfigWriter(BridgeRelayConfig config) {
        this.config = config;
    }

    @Override
    public void writeConfig(BufferedWriter writer) throws IOException {
        writeWebtunnelConfig(writer);
    }

    private void writeWebtunnelConfig(BufferedWriter writer) throws IOException {
        config.writeConfig(writer, "webtunnel", "/usr/local/bin/webtunnel", "127.0.0.1:15000", "ServerTransportOptions webtunnel url=https://" + config.getWebtunnelUrl() + "/" + config.getPath());
    }
}
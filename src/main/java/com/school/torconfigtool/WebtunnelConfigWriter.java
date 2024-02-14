package com.school.torconfigtool;

import java.io.BufferedWriter;
import java.io.IOException;

public class WebtunnelConfigWriter implements BridgeConfigWriter {
    private BridgeRelayConfig config;

    public WebtunnelConfigWriter(BridgeRelayConfig config) {
        this.config = config;
    }

    @Override
    public void writeConfig(BufferedWriter writer) throws IOException {
        config.writeWebtunnelConfig(writer);
    }
}
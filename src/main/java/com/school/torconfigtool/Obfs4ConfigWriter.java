package com.school.torconfigtool;

import java.io.BufferedWriter;
import java.io.IOException;

public class Obfs4ConfigWriter implements BridgeConfigWriter {
    private BridgeRelayConfig config;

    public Obfs4ConfigWriter(BridgeRelayConfig config) {
        this.config = config;
    }

    @Override
    public void writeConfig(BufferedWriter writer) throws IOException {
        config.writeObfs4Config(writer);
    }
}
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
        writeObfs4Config(writer);
    }

    private void writeObfs4Config(BufferedWriter writer) throws IOException {
        config.writeConfig(writer, "obfs4", "/usr/bin/obfs4proxy", "0.0.0.0:" + config.getServerTransport(), "ContactInfo " + config.getContact());
    }
}
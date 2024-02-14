package com.school.torconfigtool;

import java.io.BufferedWriter;
import java.io.IOException;

public class Obfs4ConfigWriter implements BridgeConfigWriter {
    private String serverTransport;
    private String contact;

    public Obfs4ConfigWriter(String serverTransport, String contact) {
        this.serverTransport = serverTransport;
        this.contact = contact;
    }

    @Override
    public void writeConfig(BufferedWriter writer) throws IOException {
        writer.write("ServerTransportPlugin obfs4 exec /usr/bin/obfs4proxy");
        writer.newLine();
        writer.write("ServerTransportListenAddr obfs4 0.0.0.0:" + serverTransport);
        writer.newLine();
        writer.write("ExtORPort auto");
        writer.newLine();
        writer.write("ContactInfo " + contact);
        writer.newLine();
    }
}
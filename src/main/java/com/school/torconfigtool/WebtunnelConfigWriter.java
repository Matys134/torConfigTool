package com.school.torconfigtool;

import java.io.BufferedWriter;
import java.io.IOException;

public class WebtunnelConfigWriter implements BridgeConfigWriter {
    private String webtunnelUrl;
    private String path;

    public WebtunnelConfigWriter(String webtunnelUrl, String path) {
        this.webtunnelUrl = webtunnelUrl;
        this.path = path;
    }

    @Override
    public void writeConfig(BufferedWriter writer) throws IOException {
        writer.write("ServerTransportPlugin webtunnel exec /usr/local/bin/webtunnel");
        writer.newLine();
        writer.write("ServerTransportListenAddr webtunnel 127.0.0.1:15000");
        writer.newLine();
        writer.write("ExtORPort auto");
        writer.newLine();
        writer.write("ServerTransportOptions webtunnel url=https://" + webtunnelUrl + "/" + path);
        writer.newLine();
    }
}
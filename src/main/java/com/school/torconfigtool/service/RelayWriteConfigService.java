package com.school.torconfigtool.service;

import com.school.torconfigtool.model.BridgeConfig;
import com.school.torconfigtool.model.GuardConfig;
import com.school.torconfigtool.model.RelayConfig;

import java.io.BufferedWriter;
import java.io.IOException;

public class RelayWriteConfigService {

    private final SnowflakeProxyService snowflakeProxyService = new SnowflakeProxyService();

    public void writeConfig(RelayConfig config, BufferedWriter writer) throws IOException {
        if (config instanceof BridgeConfig) {
            writeBridgeConfig((BridgeConfig) config, writer);
        } else if (config instanceof GuardConfig) {
            writeGuardConfig();
        } else {
            throw new IllegalArgumentException("Unknown relay type");
        }
    }

    private void writeBridgeConfig(BridgeConfig config, BufferedWriter writer) throws IOException {
        writer.write("BridgeRelay 1");
        writer.newLine();
        switch (config.getBridgeType()) {
            case "obfs4":
                writer.write("ServerTransportPlugin obfs4 exec /usr/bin/obfs4proxy");
                writer.newLine();
                writer.write("ServerTransportListenAddr obfs4 0.0.0.0:" + config.getServerTransport());
                writer.newLine();
                writer.write("ExtORPort auto");
                writer.newLine();
                break;
            case "webtunnel":
                writer.write("ServerTransportPlugin webtunnel exec /usr/local/bin/webtunnel");
                writer.newLine();
                writer.write("ServerTransportListenAddr webtunnel 127.0.0.1:15000");
                writer.newLine();
                writer.write("ServerTransportOptions webtunnel url=https://" + config.getWebtunnelUrl() + "/" + config.getPath());
                writer.newLine();
                writer.write("ExtORPort auto");
                writer.newLine();
                break;
            case "snowflake":
                snowflakeProxyService.setupSnowflakeProxy();
                break;
            default:
                throw new IllegalArgumentException("Unknown bridge type");
        }
    }

    private void writeGuardConfig() {

    }
}
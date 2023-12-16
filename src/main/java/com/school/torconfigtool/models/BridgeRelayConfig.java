package com.school.torconfigtool.models;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;

@Data
@EqualsAndHashCode(callSuper = true)
public class BridgeRelayConfig extends BaseRelayConfig {
    private String bridgeTransportListenAddr;
    private String relayType;
    private String webtunnelDomain;
    private int webtunnelPort;
    private String webtunnelUrl;
    private String email;
    private String bridgeType;

    @Override
    public void writeSpecificConfig(BufferedWriter writer) throws IOException {
        writer.write("BridgeRelay 1");
        writer.newLine();
        switch (getBridgeType()) {
            case "bridge":
                writer.write("ServerTransportPlugin obfs4 exec /usr/bin/obfs4proxy");
                writer.newLine();
                writer.write("ServerTransportListenAddr obfs4 0.0.0.0:" + getBridgeTransportListenAddr());
                writer.newLine();
                writer.write("ExtORPort auto");
                writer.newLine();
                writer.write("ContactInfo " + getContact());
                writer.newLine();
                // ... Add relevant configuration lines
                break;
            case "webtunnel":
                writer.write("ServerTransportPlugin webtunnel exec /usr/local/bin/webtunnel");
                writer.newLine();
                writer.write("ServerTransportListenAddr webtunnel 127.0.0.1:15000");
                writer.newLine();
                writer.write("ServerTransportOptions webtunnel url=" + getWebtunnelUrl());
                writer.newLine();
                writer.write("ExtORPort auto");
                writer.newLine();
                writer.write("ContactInfo " + getEmail());
                writer.newLine();
                writer.write("SocksPort 0");
                writer.newLine();
                // ... Add relevant configuration lines
                break;
            case "snowflake":
                runSnowflakeProxy();
                return;
        }
    }

    public void runSnowflakeProxy() {
        try {
            // Command to clone the snowflake repository
            ProcessBuilder gitCloneProcessBuilder = new ProcessBuilder("git", "clone", "https://gitlab.torproject.org/tpo/anti-censorship/pluggable-transports/snowflake.git");
            gitCloneProcessBuilder.redirectErrorStream(true);
            Process gitCloneProcess = gitCloneProcessBuilder.start();
            gitCloneProcess.waitFor();

            // Command to build the snowflake proxy
            ProcessBuilder goBuildProcessBuilder = new ProcessBuilder("go", "build");
            goBuildProcessBuilder.directory(new File("snowflake/proxy"));
            goBuildProcessBuilder.redirectErrorStream(true);
            Process goBuildProcess = goBuildProcessBuilder.start();
            goBuildProcess.waitFor();

            // Command to run the snowflake proxy
            ProcessBuilder runProxyProcessBuilder = new ProcessBuilder("nohup", "./proxy", "&");
            runProxyProcessBuilder.directory(new File("snowflake/proxy"));
            runProxyProcessBuilder.redirectErrorStream(true);
            Process runProxyProcess = runProxyProcessBuilder.start();
            runProxyProcess.waitFor();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
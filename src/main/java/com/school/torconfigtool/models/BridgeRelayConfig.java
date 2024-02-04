package com.school.torconfigtool.models;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private String path;
    private String bridgeType;
    private static final Logger logger = LoggerFactory.getLogger(BridgeRelayConfig.class);
    private static final String TORRC_DIRECTORY_PATH = "torrc/";

    @Override
    public void writeSpecificConfig(BufferedWriter writer) throws IOException {
        writer.write("BridgeRelay 1");
        writer.newLine();
        logger.info("Bridge type in BridgeRelayConfig: " + getBridgeType());
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
                break;
            case "webtunnel":
                writer.write("ServerTransportPlugin webtunnel exec /usr/local/bin/webtunnel");
                writer.newLine();
                writer.write("ServerTransportListenAddr webtunnel 127.0.0.1:15000");
                writer.newLine();
                writer.write("ServerTransportOptions webtunnel url=https://" + getWebtunnelUrl() + "/" + getPath());
                writer.newLine();
                writer.write("ExtORPort auto");
                writer.newLine();
                break;
            case "snowflake":
                runSnowflakeProxy();
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
            Process runProxyProcess = getProcess();
            runProxyProcess.waitFor();

            File snowflakeProxyRunningFile = new File(TORRC_DIRECTORY_PATH, "snowflake_proxy_running");
            if (!snowflakeProxyRunningFile.createNewFile()) {
                logger.error("Failed to create file: " + snowflakeProxyRunningFile.getAbsolutePath());
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private Process getProcess() throws IOException, InterruptedException {
        ProcessBuilder goBuildProcessBuilder = new ProcessBuilder("go", "build");
        goBuildProcessBuilder.directory(new File("snowflake/proxy"));
        goBuildProcessBuilder.redirectErrorStream(true);
        Process goBuildProcess = goBuildProcessBuilder.start();
        goBuildProcess.waitFor();

        // Command to run the snowflake proxy
        ProcessBuilder runProxyProcessBuilder = new ProcessBuilder("nohup", "./proxy", "&");
        runProxyProcessBuilder.directory(new File("snowflake/proxy"));
        runProxyProcessBuilder.redirectErrorStream(true);
        return runProxyProcessBuilder.start();
    }

    public void setBridgeType(String bridgeType) {
        this.bridgeType = bridgeType;
        logger.info("Bridge type set in BridgeRelayConfig: " + this.bridgeType);
    }
}
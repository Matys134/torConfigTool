package com.school.torconfigtool;

import com.school.torconfigtool.config.BaseRelayConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;

@Data
@EqualsAndHashCode(callSuper = true)
public class BridgeRelayConfig extends BaseRelayConfig {
    private String serverTransport;
    private String relayType;
    private String webtunnelDomain;
    private int webtunnelPort;
    private String webtunnelUrl;
    private String email;
    private String path;
    private String bridgeType;
    private static final Logger logger = LoggerFactory.getLogger(BridgeRelayConfig.class);
    private static final String TORRC_DIRECTORY_PATH = "torrc/";

    private SnowflakeProxyRunner snowflakeProxyRunner = new SnowflakeProxyRunner();

    @Override
    public void writeSpecificConfig(BufferedWriter writer) throws IOException {
        writer.write("BridgeRelay 1");
        writer.newLine();
        logger.info("Writing specific config for bridge type: " + getBridgeType());
        switch (getBridgeType()) {
            case "obfs4":
                writeObfs4Config(writer);
                break;
            case "webtunnel":
                writeWebtunnelConfig(writer);
                break;
            case "snowflake":
                writeSnowflakeConfig();
                break;
            default:
                logger.error("Unknown bridge type: " + getBridgeType());
        }
    }

    private void writeConfig(BufferedWriter writer, String bridgeType, String execCommand, String listenAddress, String additionalOptions) throws IOException {
        writer.write("ServerTransportPlugin " + bridgeType + " exec " + execCommand);
        writer.newLine();
        writer.write("ServerTransportListenAddr " + bridgeType + " " + listenAddress);
        writer.newLine();
        writer.write("ExtORPort auto");
        writer.newLine();
        if (additionalOptions != null) {
            writer.write(additionalOptions);
            writer.newLine();
        }
    }

    private void writeObfs4Config(BufferedWriter writer) throws IOException {
        writeConfig(writer, "obfs4", "/usr/bin/obfs4proxy", "0.0.0.0:" + getServerTransport(), "ContactInfo " + getContact());
    }

    private void writeWebtunnelConfig(BufferedWriter writer) throws IOException {
        writeConfig(writer, "webtunnel", "/usr/local/bin/webtunnel", "127.0.0.1:15000", "ServerTransportOptions webtunnel url=https://" + getWebtunnelUrl() + "/" + getPath());
    }

    private void writeSnowflakeConfig() {
        snowflakeProxyRunner.runSnowflakeProxy();
    }

    public void setBridgeType(String bridgeType) {
        this.bridgeType = bridgeType;
        logger.info("Bridge type set in BridgeRelayConfig: " + this.bridgeType);
    }

    public void setWebtunnelUrl(String webtunnelUrl) {
        this.webtunnelUrl = webtunnelUrl;
        logger.info("Webtunnel URL set in BridgeRelayConfig: " + this.webtunnelUrl);
    }
}
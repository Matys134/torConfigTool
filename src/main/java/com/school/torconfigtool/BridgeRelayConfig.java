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
    private SnowflakeProxyService snowflakeProxyService = new SnowflakeProxyService();

    @Override
    public void writeSpecificConfig(BufferedWriter writer) throws IOException {
        writer.write("BridgeRelay 1");
        writer.newLine();
        logger.info("Writing specific config for bridge type: " + getBridgeType());
        switch (getBridgeType()) {
            case "obfs4":
                writer.write("ServerTransportPlugin obfs4 exec /usr/bin/obfs4proxy");
                writer.newLine();
                writer.write("ServerTransportListenAddr obfs4 0.0.0.0:" + getServerTransport());
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
                snowflakeProxyService.runSnowflakeProxy();
                break;
                default:
                    logger.error("Unknown bridge type: " + getBridgeType());
        }
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
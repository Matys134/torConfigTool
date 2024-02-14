package com.school.torconfigtool;

import com.school.torconfigtool.config.BaseRelayConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class BridgeRelayConfig extends BaseRelayConfig {
    private String serverTransport;
    private String relayType;
    private String webtunnelDomain;
    private int webtunnelPort;
    private String webtunnelUrl;
    private String contact;
    private String path;
    private String bridgeType;
    private static final Logger logger = LoggerFactory.getLogger(BridgeRelayConfig.class);
    private static final String TORRC_DIRECTORY_PATH = "torrc/";

    private SnowflakeProxyRunner snowflakeProxyRunner = new SnowflakeProxyRunner();

    private Map<String, BridgeConfigWriter> configWriters;

    public BridgeRelayConfig() {
        configWriters = new HashMap<>();
        configWriters.put("obfs4", new Obfs4ConfigWriter(serverTransport, contact));
        configWriters.put("webtunnel", new WebtunnelConfigWriter(webtunnelUrl, path));
        configWriters.put("snowflake", new SnowflakeConfigWriter(snowflakeProxyRunner));
    }

    @Override
    public void writeSpecificConfig(BufferedWriter writer) throws IOException {
        BridgeConfigWriter configWriter = configWriters.get(getBridgeType());
        if (configWriter != null) {
            configWriter.writeConfig(writer);
        } else {
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
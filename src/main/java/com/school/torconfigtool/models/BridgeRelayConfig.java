package com.school.torconfigtool.models;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.BufferedWriter;
import java.io.IOException;

@Data
@EqualsAndHashCode(callSuper = true)
public class BridgeRelayConfig extends BaseRelayConfig {
    private String bridgeTransportListenAddr;
    private String relayType;
    private String webtunnelDomain;
    private String bridgeType;


    @Override
    public void writeSpecificConfig(BufferedWriter writer) throws IOException {
        writer.write("ServerTransportListenAddr obfs4 " + getBridgeTransportListenAddr());
        writer.newLine();
    }
}
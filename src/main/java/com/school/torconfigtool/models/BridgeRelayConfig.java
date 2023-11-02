package com.school.torconfigtool.models;

public class BridgeRelayConfig extends GuardRelayConfig {

    private String bridgeTransportListenAddr;
    private String relayType;

    public BridgeRelayConfig() {
    }

    public void setBridgeTransportListenAddr(String bridgeTransportListenAddr) {
        this.bridgeTransportListenAddr = bridgeTransportListenAddr;
    }

    public void setRelayType(String relayType) {
        this.relayType = relayType;
    }


    public String getBridgeTransportListenAddr() {
        return bridgeTransportListenAddr;
    }

    public String getRelayType() {
        return relayType;
    }
}

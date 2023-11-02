package com.school.torconfigtool.models;

public class BridgeRelayConfig {
    private String nickname;
    private String orPort;
    private String contact;
    private String controlPort;
    private String socksPort;
    private String bridgeTransportListenAddr;
    private String relayType;

    public BridgeRelayConfig() {
    }

    public void setControlPort(String controlPort) {
        this.controlPort = controlPort;
    }

    public void setSocksPort(String socksPort) {
        this.socksPort = socksPort;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setOrPort(String orPort) {
        this.orPort = orPort;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public void setBridgeTransportListenAddr(String bridgeTransportListenAddr) {
        this.bridgeTransportListenAddr = bridgeTransportListenAddr;
    }

    public void setRelayType(String relayType) {
        this.relayType = relayType;
    }

    public String getNickname() {
        return nickname;
    }

    public String getOrPort() {
        return orPort;
    }

    public String getContact() {
        return contact;
    }

    public String getControlPort() {
        return controlPort;
    }

    public String getSocksPort() {
        return socksPort;
    }

    public String getBridgeTransportListenAddr() {
        return bridgeTransportListenAddr;
    }

    public String getRelayType() {
        return relayType;
    }
}

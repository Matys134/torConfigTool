package com.school.torconfigtool.models;

import lombok.Data;

import java.io.BufferedWriter;
import java.io.IOException;

@Data
public abstract class BaseRelayConfig implements RelayConfig {
    private String nickname;
    private String orPort;
    private String contact;
    private String controlPort;
    private String bandwidthRate;

    @Override
    public abstract void writeSpecificConfig(BufferedWriter writer) throws IOException;

    // Add other common attributes and methods here...
}
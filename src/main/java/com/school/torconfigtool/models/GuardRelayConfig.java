package com.school.torconfigtool.models;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.BufferedWriter;
import java.io.IOException;

@Data
@EqualsAndHashCode(callSuper = true)
public class GuardRelayConfig extends BaseRelayConfig {

    @Override
    public void writeSpecificConfig(BufferedWriter writer) throws IOException {
        // Write guard-specific configurations to the file
    }
}

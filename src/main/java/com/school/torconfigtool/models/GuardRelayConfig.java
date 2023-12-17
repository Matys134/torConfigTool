package com.school.torconfigtool.models;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.BufferedWriter;

@Data
@EqualsAndHashCode(callSuper = true)
public class GuardRelayConfig extends BaseRelayConfig {

    @Override
    public void writeSpecificConfig(BufferedWriter writer) {
        // Write guard-specific configurations to the file
    }
}

package com.school.torconfigtool;

import com.school.torconfigtool.config.BaseRelayConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.BufferedWriter;

@Data
@EqualsAndHashCode(callSuper = true)
public class GuardRelayConfig extends BaseRelayConfig {

    @Override
    public void writeSpecificConfig(BufferedWriter writer) {
    }
}

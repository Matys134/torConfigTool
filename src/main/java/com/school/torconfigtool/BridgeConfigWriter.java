package com.school.torconfigtool;

import java.io.BufferedWriter;
import java.io.IOException;

public interface BridgeConfigWriter {
    void writeConfig(BufferedWriter writer) throws IOException;
}
package com.school.torconfigtool.models;

import lombok.Data;

@Data
public class RelayData {
    private double download;
    private double upload;
    private double bandwidth;
    private double uptime;
}

package com.school.torconfigtool.models;

import lombok.Data;

import java.util.List;

@Data
public class RelayData {
    private String nickname;
    private double download;
    private double upload;
    private double bandwidth;
    private double uptime;
    private List<String> flags;
    private String event;
}
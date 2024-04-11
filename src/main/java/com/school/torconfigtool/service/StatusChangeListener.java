package com.school.torconfigtool.service;

import java.io.IOException;

public interface StatusChangeListener {
    void onStatusChange(String status) throws IOException;
}
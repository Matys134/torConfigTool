package com.school.torconfigtool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class SnowflakeProxyRunner {
    private static final Logger logger = LoggerFactory.getLogger(SnowflakeProxyRunner.class);
    private static final String TORRC_DIRECTORY_PATH = "torrc/";

    public void runSnowflakeProxy() {
        try {
            cloneRepository();
            buildProxy();
            runProxy();
            createRunningFile();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void cloneRepository() throws IOException, InterruptedException {
        ProcessBuilder gitCloneProcessBuilder = new ProcessBuilder("git", "clone", "https://gitlab.torproject.org/tpo/anti-censorship/pluggable-transports/snowflake.git");
        gitCloneProcessBuilder.redirectErrorStream(true);
        Process gitCloneProcess = gitCloneProcessBuilder.start();
        gitCloneProcess.waitFor();
    }

    private void buildProxy() throws IOException, InterruptedException {
        ProcessBuilder goBuildProcessBuilder = new ProcessBuilder("go", "build");
        goBuildProcessBuilder.directory(new File("snowflake/proxy"));
        goBuildProcessBuilder.redirectErrorStream(true);
        Process goBuildProcess = goBuildProcessBuilder.start();
        goBuildProcess.waitFor();
    }

    private void runProxy() throws IOException {
        ProcessBuilder runProxyProcessBuilder = new ProcessBuilder("nohup", "./proxy", "&");
        runProxyProcessBuilder.directory(new File("snowflake/proxy"));
        runProxyProcessBuilder.redirectErrorStream(true);
        runProxyProcessBuilder.start();
    }

    private void createRunningFile() throws IOException {
        File snowflakeProxyRunningFile = new File(TORRC_DIRECTORY_PATH, "snowflake_proxy_running");
        if (!snowflakeProxyRunningFile.createNewFile()) {
            logger.error("Failed to create file: " + snowflakeProxyRunningFile.getAbsolutePath());
        }
    }
}
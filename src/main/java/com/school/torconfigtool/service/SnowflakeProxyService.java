package com.school.torconfigtool.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

/**
 * Service class for running the Snowflake proxy.
 */
@Service
public class SnowflakeProxyService {
    private static final Logger logger = LoggerFactory.getLogger(SnowflakeProxyService.class);
    private static final String TORRC_DIRECTORY_PATH = "torrc/";

    /**
     * Runs the Snowflake proxy.
     */
    public void runSnowflakeProxy() {
        try {
            // Command to clone the snowflake repository
            ProcessBuilder gitCloneProcessBuilder = new ProcessBuilder("git", "clone", "https://gitlab.torproject.org/tpo/anti-censorship/pluggable-transports/snowflake.git");
            gitCloneProcessBuilder.redirectErrorStream(true);
            Process gitCloneProcess = gitCloneProcessBuilder.start();
            gitCloneProcess.waitFor();

            // Command to build the snowflake proxy
            Process runProxyProcess = getProcess();
            runProxyProcess.waitFor();

            File snowflakeProxyRunningFile = new File(TORRC_DIRECTORY_PATH, "snowflake_proxy_running");
            if (!snowflakeProxyRunningFile.createNewFile()) {
                logger.error("Failed to create file: " + snowflakeProxyRunningFile.getAbsolutePath());
            }

        } catch (IOException | InterruptedException e) {
            logger.error("Error running snowflake proxy", e);
        }
    }

    /**
     * Builds and runs the Snowflake proxy.
     *
     * @return The process of the running Snowflake proxy.
     * @throws IOException If an I/O error occurs.
     * @throws InterruptedException If the current thread is interrupted.
     */
    private Process getProcess() throws IOException, InterruptedException {
        ProcessBuilder goBuildProcessBuilder = new ProcessBuilder("go", "build");
        goBuildProcessBuilder.directory(new File("snowflake/proxy"));
        goBuildProcessBuilder.redirectErrorStream(true);
        Process goBuildProcess = goBuildProcessBuilder.start();
        goBuildProcess.waitFor();

        // Command to run the snowflake proxy
        ProcessBuilder runProxyProcessBuilder = new ProcessBuilder("nohup", "./proxy", "&");
        runProxyProcessBuilder.directory(new File("snowflake/proxy"));
        runProxyProcessBuilder.redirectErrorStream(true);
        return runProxyProcessBuilder.start();
    }
}
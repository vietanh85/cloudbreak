package com.sequenceiq.it.cloudbreak.newway.mock;


import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static com.sequenceiq.it.cloudbreak.newway.Mock.createTempFileFromClasspath;

import java.io.File;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.tomakehurst.wiremock.WireMockServer;

@Configuration
public class MockPoolConfiguration {

    @Bean
    public WireMockServer mockServer() {
        //get random range of ports
        int randomPort = ThreadLocalRandom.current().nextInt(8400, 8900 + 1);
        File keystoreFile = createTempFileFromClasspath("/keystore_server");
        WireMockServer server = new WireMockServer(options().dynamicPort().httpsPort(randomPort).keystorePath(keystoreFile.getPath())
                .keystorePassword("secret"));
        server.start();
        return server;
    }
}

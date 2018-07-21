package com.sequenceiq.it.cloudbreak.newway;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import com.sequenceiq.it.IntegrationTestContext;
import com.sequenceiq.it.verification.Call;

import spark.Response;
import spark.Service;

public class Mock extends Entity {
    public static String MOCK_SERVER = "MOCK_SERVER";

    private static final Logger LOGGER = LoggerFactory.getLogger(Mock.class);


    private static final Service singletonSparkService = Service.ignite();

    private final Map<Call, Response> requestResponseMap = new HashMap<>();

    public Mock(String id) {
        super(id);
    }

    public Mock() {
        this(MOCK_SERVER);
    }

    public static Mock isCreated() {
        Mock mock = new Mock();
        mock.setCreationStrategy(Mock::startInGiven);
        return mock;
    }

    private static void startInGiven(IntegrationTestContext integrationTestContext, Entity entity) {
        Mock mock = (Mock) entity;
        singletonSparkService.port(9443);
        File keystoreFile = createTempFileFromClasspath("/keystore_server");
        singletonSparkService.secure(keystoreFile.getPath(), "secret", null, null);
        singletonSparkService.before((req, res) -> res.type("application/json"));
        singletonSparkService.after((request, response) -> mock.requestResponseMap.put(Call.fromRequest(request), response));
    }

    private static File createTempFileFromClasspath(String file) {
        try {
            InputStream sshPemInputStream = new ClassPathResource(file).getInputStream();
            File tempKeystoreFile = File.createTempFile(file, ".tmp");
            try (OutputStream outputStream = new FileOutputStream(tempKeystoreFile)) {
                IOUtils.copy(sshPemInputStream, outputStream);
            } catch (IOException e) {
                LOGGER.error("can't write " + file, e);
            }
            return tempKeystoreFile;
        } catch (IOException e) {
            throw new RuntimeException(file + " not found", e);
        }
    }
}

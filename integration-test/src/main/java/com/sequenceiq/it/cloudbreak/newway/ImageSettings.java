package com.sequenceiq.it.cloudbreak.newway;

import java.util.function.Function;

import javax.ws.rs.core.Response;

import com.sequenceiq.it.IntegrationTestContext;
import com.sequenceiq.it.cloudbreak.newway.context.TestContext;

public class ImageSettings extends AbstractCloudbreakEntity<com.sequenceiq.cloudbreak.api.model.v2.ImageSettings, Response, ImageSettings> {

    public static final String IMAGESETTINGS_REQUEST = "IMAGESETTINGS_REQUEST";

    ImageSettings(String newId) {
        super(newId);
        setRequest(new com.sequenceiq.cloudbreak.api.model.v2.ImageSettings());
    }

    ImageSettings() {
        this(IMAGESETTINGS_REQUEST);
    }

    public ImageSettings(TestContext testContext) {
        super(new com.sequenceiq.cloudbreak.api.model.v2.ImageSettings(), testContext);
    }

    public ImageSettings withImageCatalog(String imageCatalog) {
        getRequest().setImageCatalog(imageCatalog);
        return this;
    }

    public ImageSettings withImageId(String imageId) {
        getRequest().setImageId(imageId);
        return this;
    }

    public ImageSettings withOs(String os) {
        getRequest().setOs(os);
        return this;
    }

    public static Function<IntegrationTestContext, ImageSettings> getTestContextImageSettings(String key) {
        return testContext -> testContext.getContextParam(key, ImageSettings.class);
    }

    public static Function<IntegrationTestContext, ImageSettings> getTestContextImageSettings() {
        return getTestContextImageSettings(IMAGESETTINGS_REQUEST);
    }

    public static Function<IntegrationTestContext, ImageSettings> getNewImageSettings() {
        return testContext -> new ImageSettings();
    }

    public static ImageSettings request(String key) {
        return new ImageSettings(key);
    }

    public static ImageSettings request() {
        return new ImageSettings();
    }
}


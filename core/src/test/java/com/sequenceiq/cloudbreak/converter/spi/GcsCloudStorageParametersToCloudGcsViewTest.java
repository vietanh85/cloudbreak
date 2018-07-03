package com.sequenceiq.cloudbreak.converter.spi;

import org.junit.Assert;
import org.junit.Test;

import com.sequenceiq.cloudbreak.api.model.v2.filesystem.GcsCloudStorageParameters;
import com.sequenceiq.cloudbreak.cloud.model.filesystem.CloudGcsView;

public class GcsCloudStorageParametersToCloudGcsViewTest {

    private final GcsCloudStorageParametersToCloudGcsView underTest = new GcsCloudStorageParametersToCloudGcsView();

    @Test
    public void testConvert() {
        String projectIdString = "project-id";
        String serviceAccountEmailString = "service-account-email@some-project.iam.gserviceaccount.com";

        GcsCloudStorageParameters parameters = new GcsCloudStorageParameters();
        parameters.setProjectId(projectIdString);
        parameters.setServiceAccountEmail(serviceAccountEmailString);

        CloudGcsView convert = underTest.convert(parameters);

        Assert.assertEquals(projectIdString, convert.getProjectId());
        Assert.assertEquals(serviceAccountEmailString, convert.getServiceAccountEmail());
    }

    @Test
    public void testConvertWhenProjectIdNull() {
        String serviceAccountEmailString = "service-account-email@some-project.iam.gserviceaccount.com";

        GcsCloudStorageParameters parameters = new GcsCloudStorageParameters();
        parameters.setServiceAccountEmail(serviceAccountEmailString);

        CloudGcsView convert = underTest.convert(parameters);

        Assert.assertEquals("some-project", convert.getProjectId());
        Assert.assertEquals(serviceAccountEmailString, convert.getServiceAccountEmail());
    }

    @Test
    public void testConvertWhenThrowException() {
        String serviceAccountEmailString = "service-account-email@some-project.iam.gserviceaccount.comasdfasdf";

        GcsCloudStorageParameters parameters = new GcsCloudStorageParameters();
        parameters.setServiceAccountEmail(serviceAccountEmailString);

        CloudGcsView convert = underTest.convert(parameters);

        Assert.assertEquals("some-project", convert.getProjectId());
        Assert.assertEquals(serviceAccountEmailString, convert.getServiceAccountEmail());
    }

    @Test
    public void testConvertWhenShouldTrim() {
        String serviceAccountEmailString = "service-account-email@some-project.iam.gserviceaccount.com   ";

        GcsCloudStorageParameters parameters = new GcsCloudStorageParameters();
        parameters.setServiceAccountEmail(serviceAccountEmailString);

        CloudGcsView convert = underTest.convert(parameters);

        Assert.assertEquals("some-project", convert.getProjectId());
        Assert.assertEquals(serviceAccountEmailString, convert.getServiceAccountEmail());
    }
}

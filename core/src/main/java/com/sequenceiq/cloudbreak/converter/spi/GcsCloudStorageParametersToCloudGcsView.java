package com.sequenceiq.cloudbreak.converter.spi;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.api.model.v2.filesystem.GcsCloudStorageParameters;
import com.sequenceiq.cloudbreak.cloud.model.filesystem.CloudGcsView;
import com.sequenceiq.cloudbreak.controller.exception.BadRequestException;
import com.sequenceiq.cloudbreak.converter.AbstractConversionServiceAwareConverter;

@Component
public class GcsCloudStorageParametersToCloudGcsView
        extends AbstractConversionServiceAwareConverter<GcsCloudStorageParameters, CloudGcsView> {
    @Override
    public CloudGcsView convert(GcsCloudStorageParameters source) {
        CloudGcsView cloudGcsView = new CloudGcsView();
        cloudGcsView.setServiceAccountEmail(source.getServiceAccountEmail());
        cloudGcsView.setProjectId(getProjectId(source));
        return cloudGcsView;
    }

    private String getProjectId(GcsCloudStorageParameters source) {
        String projectId = source.getProjectId();
        if (projectId == null) {
            Pattern pattern = Pattern.compile("(.*)@(.*)(.iam.gserviceaccount.com)");
            Matcher matcher = pattern.matcher(source.getServiceAccountEmail().trim());

            if (matcher.find()) {
                projectId = matcher.group(2);
            }
        }
        if (projectId == null) {
            throw new BadRequestException("Project id cannot be null and cannot not be parse from the service account email.");
        }
        return projectId;
    }
}

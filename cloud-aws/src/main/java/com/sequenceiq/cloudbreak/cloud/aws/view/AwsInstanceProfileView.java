package com.sequenceiq.cloudbreak.cloud.aws.view;

import java.util.Optional;

import com.sequenceiq.cloudbreak.cloud.aws.AwsPlatformParameters;
import com.sequenceiq.cloudbreak.cloud.model.CloudFileSystem;
import com.sequenceiq.cloudbreak.cloud.model.CloudStack;

public class AwsInstanceProfileView {

    private final Optional<CloudFileSystem> cloudFileSystem;

    public AwsInstanceProfileView(CloudStack stack) {
        cloudFileSystem = stack.getFileSystem();
    }

    public boolean isInstanceProfileAvailable() {
        return cloudFileSystem.isPresent() && cloudFileSystem.get().getParameters().containsKey(AwsPlatformParameters.INSTANCE_PROFILE);
    }

    public String getInstanceProfile() {
        return String.valueOf(cloudFileSystem.get().getParameters().get(AwsPlatformParameters.INSTANCE_PROFILE));
    }

}

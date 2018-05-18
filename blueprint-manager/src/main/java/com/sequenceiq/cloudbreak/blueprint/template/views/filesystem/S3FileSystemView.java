package com.sequenceiq.cloudbreak.blueprint.template.views.filesystem;

import com.sequenceiq.cloudbreak.api.model.S3FileSystemConfiguration;
import com.sequenceiq.cloudbreak.blueprint.template.views.FileSystemConfigurationView;

public class S3FileSystemView extends FileSystemView<S3FileSystemConfiguration> {

    private final String instanceProfile;

    public S3FileSystemView(FileSystemConfigurationView fileSystemConfigurationView) {
        super(fileSystemConfigurationView);
        S3FileSystemConfiguration s3Config = (S3FileSystemConfiguration) fileSystemConfigurationView.getFileSystemConfiguration();
        instanceProfile = s3Config.getInstanceProfile();
    }

    @Override
    public String defaultFsValue(S3FileSystemConfiguration fileSystemConfiguration) {
        return String.format("s3://");
    }

    public String getInstanceProfile() {
        return instanceProfile;
    }
}

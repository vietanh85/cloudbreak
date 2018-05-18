package com.sequenceiq.cloudbreak.blueprint.filesystem.s3;

import static com.sequenceiq.cloudbreak.api.model.FileSystemType.S3;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.api.model.FileSystemType;
import com.sequenceiq.cloudbreak.api.model.GcsFileSystemConfiguration;
import com.sequenceiq.cloudbreak.blueprint.filesystem.AbstractFileSystemConfigurator;

@Component
public class S3FileSystemConfigurator extends AbstractFileSystemConfigurator<GcsFileSystemConfiguration> {

    private static final Logger LOGGER = LoggerFactory.getLogger(S3FileSystemConfigurator.class);

    @Override
    public FileSystemType getFileSystemType() {
        return S3;
    }
}

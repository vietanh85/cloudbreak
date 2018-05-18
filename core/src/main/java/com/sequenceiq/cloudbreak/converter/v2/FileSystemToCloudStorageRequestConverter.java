package com.sequenceiq.cloudbreak.converter.v2;

import com.sequenceiq.cloudbreak.api.model.filesystem.FileSystemResolver;
import com.sequenceiq.cloudbreak.api.model.v2.CloudStorageRequest;
import com.sequenceiq.cloudbreak.converter.AbstractConversionServiceAwareConverter;
import com.sequenceiq.cloudbreak.domain.FileSystem;

public class FileSystemToCloudStorageRequestConverter extends AbstractConversionServiceAwareConverter<FileSystem, CloudStorageRequest> {

    @Override
    public CloudStorageRequest convert(FileSystem source) {
        CloudStorageRequest request = new CloudStorageRequest();
        request.setDescription(source.getDescription());
        FileSystemResolver.setFSV2RequestFileSystemParamsByNameAndProperties(source.getType(), source.getProperties(), request);
        return request;
    }

}

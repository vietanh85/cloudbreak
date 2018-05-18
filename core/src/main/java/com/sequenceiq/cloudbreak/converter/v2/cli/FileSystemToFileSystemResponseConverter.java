package com.sequenceiq.cloudbreak.converter.v2.cli;

import java.io.IOException;
import java.util.HashSet;

import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.api.model.FileSystemResponse;
import com.sequenceiq.cloudbreak.api.model.filesystem.AdlsFileSystem;
import com.sequenceiq.cloudbreak.api.model.filesystem.GcsFileSystem;
import com.sequenceiq.cloudbreak.api.model.filesystem.S3FileSystem;
import com.sequenceiq.cloudbreak.api.model.filesystem.WasbFileSystem;
import com.sequenceiq.cloudbreak.api.model.v2.StorageLocationResponse;
import com.sequenceiq.cloudbreak.api.model.v2.filesystem.AdlsCloudStorageParameters;
import com.sequenceiq.cloudbreak.api.model.v2.filesystem.GcsCloudStorageParameters;
import com.sequenceiq.cloudbreak.api.model.v2.filesystem.S3CloudStorageParameters;
import com.sequenceiq.cloudbreak.api.model.v2.filesystem.WasbCloudStorageParameters;
import com.sequenceiq.cloudbreak.converter.AbstractConversionServiceAwareConverter;
import com.sequenceiq.cloudbreak.domain.FileSystem;
import com.sequenceiq.cloudbreak.domain.StorageLocation;
import com.sequenceiq.cloudbreak.domain.StorageLocations;

@Component
public class FileSystemToFileSystemResponseConverter extends AbstractConversionServiceAwareConverter<FileSystem, FileSystemResponse> {

    @Override
    public FileSystemResponse convert(FileSystem source) {
        FileSystemResponse response = new FileSystemResponse();
        response.setId(source.getId());
        response.setName(source.getName());
        response.setDefaultFs(source.isDefaultFs());
        try {
            StorageLocations storageLocations = source.getLocations() == null ? new StorageLocations() : source.getLocations().get(StorageLocations.class);
            for (StorageLocation storageLocation : storageLocations.getLocations()) {
                response.getLocations().add(getConversionService().convert(storageLocation, StorageLocationResponse.class));
            }
        } catch (IOException e) {
            response.setLocations(new HashSet<>());
        }
        try {
            if (source.getType().isAdls()) {
                response.setAdls(getConversionService().convert(source.getConfigurations().get(AdlsFileSystem.class), AdlsCloudStorageParameters.class));
            } else if (source.getType().isGcs()) {
                response.setGcs(getConversionService().convert(source.getConfigurations().get(GcsFileSystem.class), GcsCloudStorageParameters.class));
            } else if (source.getType().isS3()) {
                response.setS3(getConversionService().convert(source.getConfigurations().get(S3FileSystem.class), S3CloudStorageParameters.class));
            } else if (source.getType().isWasb()) {
                response.setWasb(getConversionService().convert(source.getConfigurations().get(WasbFileSystem.class), WasbCloudStorageParameters.class));
            }
        } catch (IOException e) {

        }
        response.setType(source.getType().name());
        return response;
    }

}

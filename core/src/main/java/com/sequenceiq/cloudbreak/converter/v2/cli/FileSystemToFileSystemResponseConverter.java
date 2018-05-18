package com.sequenceiq.cloudbreak.converter.v2.cli;

import java.io.IOException;
import java.util.HashSet;

import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.api.model.FileSystemResponse;
import com.sequenceiq.cloudbreak.api.model.FileSystemType;
import com.sequenceiq.cloudbreak.api.model.v2.StorageLocationResponse;
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
        response.setType(FileSystemType.valueOf(source.getType()));
        response.setDefaultFs(source.isDefaultFs());
        response.setProperties(source.getProperties());
        try {
            StorageLocations storageLocations = source.getLocations() == null ? new StorageLocations() : source.getLocations().get(StorageLocations.class);
            for (StorageLocation storageLocation : storageLocations.getLocations()) {
                response.getLocations().add(getConversionService().convert(storageLocation, StorageLocationResponse.class));
            }
        } catch (IOException e) {
            response.setLocations(new HashSet<>());
        }
        return response;
    }

}

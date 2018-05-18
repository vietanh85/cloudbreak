package com.sequenceiq.cloudbreak.converter;

import static com.sequenceiq.cloudbreak.common.type.APIResourceType.FILESYSTEM;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sequenceiq.cloudbreak.api.model.FileSystemRequest;
import com.sequenceiq.cloudbreak.api.model.v2.StorageLocationRequest;
import com.sequenceiq.cloudbreak.controller.exception.BadRequestException;
import com.sequenceiq.cloudbreak.domain.FileSystem;
import com.sequenceiq.cloudbreak.domain.StorageLocation;
import com.sequenceiq.cloudbreak.domain.StorageLocations;
import com.sequenceiq.cloudbreak.domain.json.Json;
import com.sequenceiq.cloudbreak.service.MissingResourceNameGenerator;

@Component
public class FileSystemRequestToFileSystemConverter extends AbstractConversionServiceAwareConverter<FileSystemRequest, FileSystem> {

    @Inject
    private MissingResourceNameGenerator nameGenerator;

    @Override
    public FileSystem convert(FileSystemRequest source) {
        FileSystem fs = new FileSystem();
        fs.setName(nameGenerator.generateName(FILESYSTEM));
        fs.setType(source.getType().name());
        fs.setDefaultFs(source.isDefaultFs());
        if (source.getProperties() != null) {
            fs.setProperties(source.getProperties());
        } else {
            fs.setProperties(new HashMap<>());
        }
        Set<StorageLocation> locations = new HashSet<>();
        for (StorageLocationRequest storageLocationRequest : source.getLocations()) {
            locations.add(getConversionService().convert(storageLocationRequest, StorageLocation.class));
        }
        try {
            StorageLocations storageLocations = new StorageLocations();
            storageLocations.setLocations(locations);
            fs.setLocations(new Json(storageLocations));
        } catch (JsonProcessingException e) {
            throw new BadRequestException("Storage locations could not be parsed: " + source);
        }
        return fs;
    }
}

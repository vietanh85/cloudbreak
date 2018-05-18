package com.sequenceiq.cloudbreak.converter;

import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.api.model.v2.StorageLocationRequest;
import com.sequenceiq.cloudbreak.domain.StorageLocation;

@Component
public class StorageLocationRequestToStorageLocationConverter extends AbstractConversionServiceAwareConverter<StorageLocationRequest, StorageLocation> {

    @Override
    public StorageLocation convert(StorageLocationRequest source) {
        StorageLocation storageLocation = new StorageLocation();
        storageLocation.setConfigFile(source.getConfigFile());
        storageLocation.setProperty(source.getProperty());
        storageLocation.setValue(source.getValue());
        return storageLocation;
    }
}

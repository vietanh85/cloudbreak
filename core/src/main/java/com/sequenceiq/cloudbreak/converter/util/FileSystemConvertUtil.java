package com.sequenceiq.cloudbreak.converter.util;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.domain.FileSystem;
import com.sequenceiq.cloudbreak.domain.StorageLocation;
import com.sequenceiq.cloudbreak.domain.StorageLocationCategory;
import com.sequenceiq.cloudbreak.domain.StorageLocations;

@Component
public class FileSystemConvertUtil {

    public void populateStorageLocationsFromMap(StorageLocationCategory sourceCategory, Map<String, String> sourceLocations,
            Set<StorageLocation> targetLocations) {
        if (sourceLocations != null) {
            for (Map.Entry<String, String> sourceLocationEntry : sourceLocations.entrySet()) {
                StorageLocation targetLocation = new StorageLocation();
                targetLocation.setId(sourceLocationEntry.getKey());
                targetLocation.setCategory(sourceCategory);
                // TODO set StorageLocation configFile and property
                targetLocation.setValue(sourceLocationEntry.getValue());
                targetLocations.add(targetLocation);
            }
        }
    }

    public void populateStorageLocationsFromFileSystem(FileSystem source, Map<String, String> targetOpLogs, Map<String, String> targetNotebook,
            Map<String, String> targetWarehouse, Map<String, String> targetAudit) throws IOException {
        if (source.getLocations() != null && source.getLocations().getValue() != null) {
            StorageLocations storageLocations = source.getLocations().get(StorageLocations.class);
            if (storageLocations != null) {
                for (StorageLocation storageLocation : storageLocations.getLocations()) {
                    Map<String, String> locations;
                    // TODO set StorageLocation id and category for legacy entries
                    switch (storageLocation.getCategory()) {
                        case OP_LOGS:
                            locations = targetOpLogs;
                            break;
                        case NOTEBOOK:
                            locations = targetNotebook;
                            break;
                        case WAREHOUSE:
                            locations = targetWarehouse;
                            break;
                        case AUDIT:
                            locations = targetAudit;
                            break;
                        default:
                            throw new IllegalStateException("Unhandled StorageLocationCategory: " + storageLocation.getCategory());
                    }
                    locations.put(storageLocation.getId(), storageLocation.getValue());
                }
            }
        }
    }

}

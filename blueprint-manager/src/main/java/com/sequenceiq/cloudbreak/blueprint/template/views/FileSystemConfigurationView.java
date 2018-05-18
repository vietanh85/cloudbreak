package com.sequenceiq.cloudbreak.blueprint.template.views;

import java.util.HashSet;
import java.util.Set;

import com.sequenceiq.cloudbreak.api.model.FileSystemConfiguration;
import com.sequenceiq.cloudbreak.blueprint.filesystem.StorageLocationView;
import com.sequenceiq.cloudbreak.domain.StorageLocation;
import com.sequenceiq.cloudbreak.domain.StorageLocations;

public class FileSystemConfigurationView {

    private final FileSystemConfiguration fileSystemConfiguration;

    private final boolean defaultFs;

    private final Set<StorageLocationView> locations;

    public FileSystemConfigurationView(FileSystemConfiguration fileSystemConfiguration, StorageLocations storageLocations) {
        this.fileSystemConfiguration = fileSystemConfiguration;
        Set<StorageLocationView> storageLocationViews = new HashSet<>();
        if (storageLocations == null || storageLocations.getLocations() == null) {
            this.locations = new HashSet<>();
        } else {
            for (StorageLocation storageLocation : storageLocations.getLocations()) {
                storageLocationViews.add(new StorageLocationView(storageLocation));
            }
            this.locations = storageLocationViews;
        }
        this.defaultFs = false;
    }

    public FileSystemConfiguration getFileSystemConfiguration() {
        return fileSystemConfiguration;
    }

    public boolean isDefaultFs() {
        return defaultFs;
    }

    public Set<StorageLocationView> getLocations() {
        return locations;
    }
}

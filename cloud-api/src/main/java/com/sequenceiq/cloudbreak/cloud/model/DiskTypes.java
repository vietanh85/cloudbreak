package com.sequenceiq.cloudbreak.cloud.model;

import java.util.Collection;
import java.util.Map;

import com.sequenceiq.cloudbreak.cloud.model.generic.CloudTypes;

/**
 * Types of disks of a platform
 *
 * @see CloudTypes
 * @see DiskType
 */
public class DiskTypes extends CloudTypes<DiskType> {

    private final Map<String, VolumeParameterType> diskMapping;

    private final Map<DiskType, DisplayName> displayNames;

    public DiskTypes(Collection<DiskType> diskTypes, DiskType defaultDiskType, Map<String, VolumeParameterType> diskMapping,
            Map<DiskType, DisplayName> displayNames) {
        super(diskTypes, defaultDiskType);
        this.diskMapping = diskMapping;
        this.displayNames = displayNames;
    }

    public Map<String, VolumeParameterType> diskMapping() {
        return diskMapping;
    }

    public Map<DiskType, DisplayName> displayNames() {
        return displayNames;
    }
}

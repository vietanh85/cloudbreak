package com.sequenceiq.cloudbreak.blueprint.template.views.filesystem;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.sequenceiq.cloudbreak.api.model.FileSystemConfiguration;
import com.sequenceiq.cloudbreak.blueprint.filesystem.StorageLocationView;
import com.sequenceiq.cloudbreak.blueprint.template.views.FileSystemConfigurationView;

public abstract class FileSystemView<T extends FileSystemConfiguration> {

    private final boolean useAsDefault;

    private final String defaultFs;

    private Map<String, String> properties;

    private final List<StorageLocationView> locations;

    public FileSystemView(FileSystemConfigurationView fileSystemConfigurationView) {
        useAsDefault = fileSystemConfigurationView.isDefaultFs();
        properties = fileSystemConfigurationView.getFileSystemConfiguration().getDynamicProperties();
        defaultFs = defaultFsValue((T) fileSystemConfigurationView.getFileSystemConfiguration());
        locations = fileSystemConfigurationView.getLocations()
                .stream()
                .sorted(Comparator.comparing(StorageLocationView::getConfigFile))
                .collect(Collectors.toList());
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public String getDefaultFs() {
        return defaultFs;
    }

    public boolean isUseAsDefault() {
        return useAsDefault;
    }

    public abstract String defaultFsValue(T fileSystemConfiguration);

    public List<StorageLocationView> getLocations() {
        return locations;
    }
}

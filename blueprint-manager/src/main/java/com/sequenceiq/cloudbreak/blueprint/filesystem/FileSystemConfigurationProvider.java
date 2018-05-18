package com.sequenceiq.cloudbreak.blueprint.filesystem;

import java.io.IOException;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.sequenceiq.cloudbreak.common.type.CloudConstants;
import com.sequenceiq.cloudbreak.common.type.ResourceType;
import com.sequenceiq.cloudbreak.domain.FileSystem;
import com.sequenceiq.cloudbreak.domain.stack.Stack;

@Service
public class FileSystemConfigurationProvider {

    @Inject
    private AzureFileSystemConfigProvider azureFileSystemConfigProvider;

    @Inject
    private FileSystemConfigurationsViewProvider fileSystemConfigurationsViewProvider;

    public BaseFileSystemConfigurationsView fileSystemConfiguration(FileSystem fs, Stack stack) throws IOException {
        BaseFileSystemConfigurationsView fileSystemConfiguration = null;
        if (fs != null && stack != null) {
            fileSystemConfiguration = fileSystemConfigurationsViewProvider.propagateConfigurationsView(fs);
            fileSystemConfiguration.setStorageContainer("cloudbreak" + stack.getId());
            if (CloudConstants.AZURE.equals(stack.getPlatformVariant())) {
                com.sequenceiq.cloudbreak.domain.Resource resourceByType = stack.getResourceByType(ResourceType.ARM_TEMPLATE);
                fileSystemConfiguration = azureFileSystemConfigProvider.decorateFileSystemConfiguration(stack.getUuid(), stack.getCredential(),
                        resourceByType, fileSystemConfiguration);
            }
        }
        return fileSystemConfiguration;
    }
}

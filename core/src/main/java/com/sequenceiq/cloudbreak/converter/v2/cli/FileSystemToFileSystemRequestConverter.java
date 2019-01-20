package com.sequenceiq.cloudbreak.converter.v2.cli;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.api.model.FileSystemRequest;
import com.sequenceiq.cloudbreak.api.model.filesystem.AdlsFileSystem;
import com.sequenceiq.cloudbreak.api.model.filesystem.AdlsGen2FileSystem;
import com.sequenceiq.cloudbreak.api.model.filesystem.GcsFileSystem;
import com.sequenceiq.cloudbreak.api.model.filesystem.S3FileSystem;
import com.sequenceiq.cloudbreak.api.model.filesystem.WasbFileSystem;
import com.sequenceiq.cloudbreak.api.model.v2.filesystem.AdlsCloudStorageParameters;
import com.sequenceiq.cloudbreak.api.model.v2.filesystem.AdlsGen2CloudStorageParameters;
import com.sequenceiq.cloudbreak.api.model.v2.filesystem.GcsCloudStorageParameters;
import com.sequenceiq.cloudbreak.api.model.v2.filesystem.S3CloudStorageParameters;
import com.sequenceiq.cloudbreak.api.model.v2.filesystem.WasbCloudStorageParameters;
import com.sequenceiq.cloudbreak.converter.AbstractConversionServiceAwareConverter;
import com.sequenceiq.cloudbreak.converter.util.FileSystemConvertUtil;
import com.sequenceiq.cloudbreak.domain.FileSystem;

@Component
public class FileSystemToFileSystemRequestConverter extends AbstractConversionServiceAwareConverter<FileSystem, FileSystemRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileSystemToFileSystemRequestConverter.class);

    @Inject
    private FileSystemConvertUtil fileSystemConvertUtil;

    @Override
    public FileSystemRequest convert(FileSystem source) {
        FileSystemRequest request = new FileSystemRequest();
        request.setName(source.getName());
        populateStorageLocations(source, request);
        try {
            if (source.getType().isAdls()) {
                request.setAdls(getConversionService().convert(source.getConfigurations().get(AdlsFileSystem.class), AdlsCloudStorageParameters.class));
            } else if (source.getType().isGcs()) {
                request.setGcs(getConversionService().convert(source.getConfigurations().get(GcsFileSystem.class), GcsCloudStorageParameters.class));
            } else if (source.getType().isS3()) {
                request.setS3(getConversionService().convert(source.getConfigurations().get(S3FileSystem.class), S3CloudStorageParameters.class));
            } else if (source.getType().isWasb()) {
                request.setWasb(getConversionService().convert(source.getConfigurations().get(WasbFileSystem.class), WasbCloudStorageParameters.class));
            } else if (source.getType().isAdlsGen2()) {
                request.setAdlsGen2(getConversionService().convert(source.getConfigurations().get(AdlsGen2FileSystem.class),
                        AdlsGen2CloudStorageParameters.class));
            }
        } catch (IOException ioe) {
            LOGGER.info("Error when attempting to obtain/convert file system configuration", ioe);
        }
        request.setType(source.getType().name());
        return request;
    }

    private void populateStorageLocations(FileSystem source, FileSystemRequest request) {
        Map<String, String> opLogs = new HashMap<>();
        Map<String, String> notebook = new HashMap<>();
        Map<String, String> warehouse = new HashMap<>();
        Map<String, String> audit = new HashMap<>();
        try {
            fileSystemConvertUtil.populateStorageLocationsFromFileSystem(source, opLogs, notebook, warehouse, audit);
        } catch (IOException ioe) {
            LOGGER.info("Error when attempting to obtain/convert file system locations", ioe);
        }
        request.setOpLogs(opLogs);
        request.setNotebook(notebook);
        request.setWarehouse(warehouse);
        request.setAudit(audit);
    }

}

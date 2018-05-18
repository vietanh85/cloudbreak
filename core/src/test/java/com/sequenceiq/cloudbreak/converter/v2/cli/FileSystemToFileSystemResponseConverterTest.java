package com.sequenceiq.cloudbreak.converter.v2.cli;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.convert.ConversionService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sequenceiq.cloudbreak.api.model.FileSystemResponse;
import com.sequenceiq.cloudbreak.api.model.filesystem.AdlsFileSystem;
import com.sequenceiq.cloudbreak.api.model.filesystem.BaseFileSystem;
import com.sequenceiq.cloudbreak.api.model.filesystem.FileSystemType;
import com.sequenceiq.cloudbreak.api.model.v2.filesystem.AdlsCloudStorageParameters;
import com.sequenceiq.cloudbreak.domain.FileSystem;
import com.sequenceiq.cloudbreak.domain.json.Json;

public class FileSystemToFileSystemResponseConverterTest {

    private static final Long FILE_SYSTEM_ID = 1L;

    private static final String FILE_SYSTEM_NAME = "fsName";

    private static final FileSystemType EXAMPLE_FILE_SYSTEM_TYPE = FileSystemType.GCS;

    private static final boolean EXAMPLE_IS_DEFAULT_FS_VALUE = true;

    @InjectMocks
    private FileSystemToFileSystemResponseConverter underTest;

    @Mock
    private ConversionService conversionService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        underTest = spy(underTest);
    }

    @Test
    public void testConvertWhenSourceContainsValidDataThenThisShouldBeConvertedIntoResponse() throws IOException {
        FileSystem fileSystem = createFileSystemSource();
        when(underTest.getConversionService()).thenReturn(conversionService);
        when(conversionService.convert(fileSystem.getConfigurations().get(BaseFileSystem.class), AdlsCloudStorageParameters.class))
                .thenReturn(new AdlsCloudStorageParameters());

        FileSystemResponse result = underTest.convert(fileSystem);

        assertEquals(FILE_SYSTEM_ID, result.getId());
        assertEquals(FILE_SYSTEM_NAME, result.getName());
        assertEquals(EXAMPLE_FILE_SYSTEM_TYPE.name(), result.getType());
        assertEquals(EXAMPLE_IS_DEFAULT_FS_VALUE, result.isDefaultFs());
    }

    private FileSystem createFileSystemSource() throws JsonProcessingException {
        FileSystem fileSystem = new FileSystem();
        fileSystem.setId(FILE_SYSTEM_ID);
        fileSystem.setName(FILE_SYSTEM_NAME);
        fileSystem.setType(EXAMPLE_FILE_SYSTEM_TYPE);
        fileSystem.setDefaultFs(EXAMPLE_IS_DEFAULT_FS_VALUE);
        fileSystem.setConfigurations(new Json(new AdlsFileSystem()));
        return fileSystem;
    }

}
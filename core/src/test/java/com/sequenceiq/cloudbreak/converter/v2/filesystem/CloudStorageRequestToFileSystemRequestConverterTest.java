package com.sequenceiq.cloudbreak.converter.v2.filesystem;

import static com.sequenceiq.cloudbreak.api.model.filesystem.FileSystemType.WASB;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sequenceiq.cloudbreak.api.model.FileSystemRequest;
import com.sequenceiq.cloudbreak.api.model.filesystem.FileSystemType;
import com.sequenceiq.cloudbreak.api.model.v2.CloudStorageRequest;
import com.sequenceiq.cloudbreak.api.model.v2.filesystem.AdlsCloudStorageParameters;
import com.sequenceiq.cloudbreak.api.model.v2.filesystem.AdlsGen2CloudStorageParameters;
import com.sequenceiq.cloudbreak.api.model.v2.filesystem.CloudStorageParameters;
import com.sequenceiq.cloudbreak.common.type.APIResourceType;
import com.sequenceiq.cloudbreak.service.MissingResourceNameGenerator;
import com.sequenceiq.cloudbreak.service.filesystem.FileSystemResolver;

public class CloudStorageRequestToFileSystemRequestConverterTest {

    private static final String TEST_FILE_SYSTEM_NAME = "fsName";

    private static final FileSystemType TEST_FILE_SYSTEM = WASB;

    private static final Map<String, String> OP_LOGS = Map.of("oK1", "oV1", "oK2", "oV2");

    private static final Map<String, String> NOTEBOOK = Map.of("nK1", "nV1", "nK2", "nV2");

    private static final Map<String, String> WAREHOUSE = Map.of("wK1", "wV1", "wK2", "wV2");

    private static final Map<String, String> AUDIT = Map.of("aK1", "aV1", "aK2", "aV2");

    @InjectMocks
    private CloudStorageRequestToFileSystemRequestConverter underTest;

    @Mock
    private MissingResourceNameGenerator nameGenerator;

    @Mock
    private FileSystemResolver fileSystemResolver;

    @Mock
    private CloudStorageRequest request;

    @Mock
    private CloudStorageParameters cloudStorageParameters;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(nameGenerator.generateName(APIResourceType.FILESYSTEM)).thenReturn(TEST_FILE_SYSTEM_NAME);
    }

    @Test
    public void testConvertCheckAllParamsArePassedProperlyWhenOneOfTheFileSystemTypeIsNotNull() {
        AdlsCloudStorageParameters adls = new AdlsCloudStorageParameters();
        when(request.getAdls()).thenReturn(adls);
        when(request.getWasb()).thenReturn(null);
        when(request.getGcs()).thenReturn(null);
        when(request.getS3()).thenReturn(null);
        when(request.getAdlsGen2()).thenReturn(null);
        when(request.getOpLogs()).thenReturn(OP_LOGS);
        when(request.getNotebook()).thenReturn(NOTEBOOK);
        when(request.getWarehouse()).thenReturn(WAREHOUSE);
        when(request.getAudit()).thenReturn(AUDIT);
        when(fileSystemResolver.propagateConfiguration(request)).thenReturn(cloudStorageParameters);
        when(cloudStorageParameters.getType()).thenReturn(TEST_FILE_SYSTEM);

        FileSystemRequest result = underTest.convert(request);

        assertEquals(TEST_FILE_SYSTEM_NAME, result.getName());
        assertFalse(result.isDefaultFs());
        assertEquals(adls, result.getAdls());
        assertNull(result.getGcs());
        assertNull(result.getS3());
        assertNull(result.getWasb());
        assertNull(result.getAdlsGen2());
        assertEquals(OP_LOGS, result.getOpLogs());
        assertEquals(NOTEBOOK, result.getNotebook());
        assertEquals(WAREHOUSE, result.getWarehouse());
        assertEquals(AUDIT, result.getAudit());
        assertEquals(TEST_FILE_SYSTEM.name(), result.getType());
        verify(nameGenerator, times(1)).generateName(APIResourceType.FILESYSTEM);
        verify(fileSystemResolver, times(1)).propagateConfiguration(request);
    }

    @Test
    public void testConvertCheckAllParamsArePassedProperlyWhenAdlsGen2IsNotNull() {
        AdlsGen2CloudStorageParameters adlsGen2 = new AdlsGen2CloudStorageParameters();
        when(request.getAdlsGen2()).thenReturn(adlsGen2);
        when(request.getAdls()).thenReturn(null);
        when(request.getWasb()).thenReturn(null);
        when(request.getGcs()).thenReturn(null);
        when(request.getS3()).thenReturn(null);
        when(request.getOpLogs()).thenReturn(OP_LOGS);
        when(request.getNotebook()).thenReturn(NOTEBOOK);
        when(request.getWarehouse()).thenReturn(WAREHOUSE);
        when(request.getAudit()).thenReturn(AUDIT);
        when(fileSystemResolver.propagateConfiguration(request)).thenReturn(cloudStorageParameters);
        when(cloudStorageParameters.getType()).thenReturn(TEST_FILE_SYSTEM);

        FileSystemRequest result = underTest.convert(request);

        assertEquals(TEST_FILE_SYSTEM_NAME, result.getName());
        assertFalse(result.isDefaultFs());
        assertEquals(adlsGen2, result.getAdlsGen2());
        assertNull(result.getGcs());
        assertNull(result.getS3());
        assertNull(result.getWasb());
        assertNull(result.getAdls());
        assertEquals(OP_LOGS, result.getOpLogs());
        assertEquals(NOTEBOOK, result.getNotebook());
        assertEquals(WAREHOUSE, result.getWarehouse());
        assertEquals(AUDIT, result.getAudit());
        assertEquals(TEST_FILE_SYSTEM.name(), result.getType());
        verify(nameGenerator, times(1)).generateName(APIResourceType.FILESYSTEM);
        verify(fileSystemResolver, times(1)).propagateConfiguration(request);
    }

}
package com.sequenceiq.cloudbreak.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;
import org.springframework.core.convert.ConversionService;

import com.sequenceiq.cloudbreak.api.model.FileSystemRequest;
import com.sequenceiq.cloudbreak.api.model.filesystem.AdlsFileSystem;
import com.sequenceiq.cloudbreak.api.model.filesystem.AdlsGen2FileSystem;
import com.sequenceiq.cloudbreak.api.model.filesystem.FileSystemType;
import com.sequenceiq.cloudbreak.api.model.filesystem.GcsFileSystem;
import com.sequenceiq.cloudbreak.api.model.filesystem.S3FileSystem;
import com.sequenceiq.cloudbreak.api.model.filesystem.WasbFileSystem;
import com.sequenceiq.cloudbreak.api.model.v2.filesystem.AdlsCloudStorageParameters;
import com.sequenceiq.cloudbreak.api.model.v2.filesystem.AdlsGen2CloudStorageParameters;
import com.sequenceiq.cloudbreak.api.model.v2.filesystem.GcsCloudStorageParameters;
import com.sequenceiq.cloudbreak.api.model.v2.filesystem.S3CloudStorageParameters;
import com.sequenceiq.cloudbreak.api.model.v2.filesystem.WasbCloudStorageParameters;
import com.sequenceiq.cloudbreak.common.type.APIResourceType;
import com.sequenceiq.cloudbreak.converter.util.FileSystemConvertUtil;
import com.sequenceiq.cloudbreak.domain.FileSystem;
import com.sequenceiq.cloudbreak.domain.StorageLocation;
import com.sequenceiq.cloudbreak.domain.StorageLocationCategory;
import com.sequenceiq.cloudbreak.service.MissingResourceNameGenerator;

public class FileSystemRequestToFileSystemConverterTest {

    private static final String LOCATIONS = "locations";

    @InjectMocks
    private FileSystemRequestToFileSystemConverter underTest;

    @Mock
    private MissingResourceNameGenerator nameGenerator;

    @Mock
    private FileSystemConvertUtil fileSystemConvertUtil;

    @Mock
    private ConversionService conversionService;

    @Mock
    private FileSystemRequest request;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testConvertNameHasBeenSetThroughGenerator() {
        String testName = "nameValue";
        when(nameGenerator.generateName(APIResourceType.FILESYSTEM)).thenReturn(testName);

        FileSystem result = underTest.convert(request);

        assertEquals(testName, result.getName());
    }

    @Test
    public void testConvertWhenAdlsIsNotNullThenItWillBeSetAsBaseFileSystem() {
        AdlsCloudStorageParameters adls = new AdlsCloudStorageParameters();
        when(request.getAdls()).thenReturn(adls);
        when(conversionService.convert(adls, AdlsFileSystem.class)).thenReturn(new AdlsFileSystem());

        FileSystem result = underTest.convert(request);

        assertNotNull(result.getConfigurations());
        assertEquals(FileSystemType.ADLS, result.getType());
        verify(conversionService, times(1)).convert(adls, AdlsFileSystem.class);
        verify(conversionService, times(0)).convert(any(GcsCloudStorageParameters.class), eq(GcsFileSystem.class));
        verify(conversionService, times(0)).convert(any(S3CloudStorageParameters.class), eq(S3FileSystem.class));
        verify(conversionService, times(0)).convert(any(WasbCloudStorageParameters.class), eq(WasbFileSystem.class));
        verify(conversionService, times(0)).convert(any(AdlsGen2CloudStorageParameters.class), eq(AdlsGen2FileSystem.class));
    }

    @Test
    public void testConvertWhenGcsIsNotNullThenItWillBeSetAsBaseFileSystem() {
        GcsCloudStorageParameters gcs = new GcsCloudStorageParameters();
        when(request.getGcs()).thenReturn(gcs);
        when(conversionService.convert(gcs, GcsFileSystem.class)).thenReturn(new GcsFileSystem());

        FileSystem result = underTest.convert(request);

        assertNotNull(result.getConfigurations());
        assertEquals(FileSystemType.GCS, result.getType());
        verify(conversionService, times(1)).convert(gcs, GcsFileSystem.class);
        verify(conversionService, times(0)).convert(any(AdlsCloudStorageParameters.class), eq(AdlsFileSystem.class));
        verify(conversionService, times(0)).convert(any(S3CloudStorageParameters.class), eq(S3FileSystem.class));
        verify(conversionService, times(0)).convert(any(WasbCloudStorageParameters.class), eq(WasbFileSystem.class));
        verify(conversionService, times(0)).convert(any(AdlsGen2CloudStorageParameters.class), eq(AdlsGen2FileSystem.class));
    }

    @Test
    public void testConvertWhenS3IsNotNullThenItWillBeSetAsBaseFileSystem() {
        S3CloudStorageParameters s3 = new S3CloudStorageParameters();
        when(request.getS3()).thenReturn(s3);
        when(conversionService.convert(s3, S3FileSystem.class)).thenReturn(new S3FileSystem());

        FileSystem result = underTest.convert(request);

        assertNotNull(result.getConfigurations());
        assertEquals(FileSystemType.S3, result.getType());
        verify(conversionService, times(1)).convert(s3, S3FileSystem.class);
        verify(conversionService, times(0)).convert(any(GcsCloudStorageParameters.class), eq(GcsFileSystem.class));
        verify(conversionService, times(0)).convert(any(AdlsCloudStorageParameters.class), eq(AdlsFileSystem.class));
        verify(conversionService, times(0)).convert(any(WasbCloudStorageParameters.class), eq(WasbFileSystem.class));
        verify(conversionService, times(0)).convert(any(AdlsGen2CloudStorageParameters.class), eq(AdlsGen2FileSystem.class));
    }

    @Test
    public void testConvertWhenWasbIsNotNullThenItWillBeSetAsBaseFileSystem() {
        WasbCloudStorageParameters wasb = new WasbCloudStorageParameters();
        when(request.getWasb()).thenReturn(wasb);
        when(conversionService.convert(wasb, WasbFileSystem.class)).thenReturn(new WasbFileSystem());

        FileSystem result = underTest.convert(request);

        assertNotNull(result.getConfigurations());
        assertEquals(FileSystemType.WASB, result.getType());
        verify(conversionService, times(1)).convert(wasb, WasbFileSystem.class);
        verify(conversionService, times(0)).convert(any(GcsCloudStorageParameters.class), eq(GcsFileSystem.class));
        verify(conversionService, times(0)).convert(any(AdlsCloudStorageParameters.class), eq(AdlsFileSystem.class));
        verify(conversionService, times(0)).convert(any(S3CloudStorageParameters.class), eq(S3FileSystem.class));
        verify(conversionService, times(0)).convert(any(AdlsGen2CloudStorageParameters.class), eq(AdlsGen2FileSystem.class));
    }

    @Test
    public void testConvertWhenAbfsIsNotNullThenItWillBeSetAsBaseFileSystem() {
        AdlsGen2CloudStorageParameters adlsGen2 = new AdlsGen2CloudStorageParameters();
        when(request.getAdlsGen2()).thenReturn(adlsGen2);
        when(conversionService.convert(adlsGen2, AdlsGen2FileSystem.class)).thenReturn(new AdlsGen2FileSystem());

        FileSystem result = underTest.convert(request);

        assertNotNull(result.getConfigurations());
        assertEquals(FileSystemType.ADLS_GEN_2, result.getType());
        verify(conversionService, times(1)).convert(adlsGen2, AdlsGen2FileSystem.class);
        verify(conversionService, times(0)).convert(any(GcsCloudStorageParameters.class), eq(GcsFileSystem.class));
        verify(conversionService, times(0)).convert(any(AdlsCloudStorageParameters.class), eq(AdlsFileSystem.class));
        verify(conversionService, times(0)).convert(any(S3CloudStorageParameters.class), eq(S3FileSystem.class));
        verify(conversionService, times(0)).convert(any(WasbCloudStorageParameters.class), eq(WasbFileSystem.class));
    }

    @Test
    public void testConvertWhenSourceHasNoStorageLocationThenLocationJsonShouldBeEmpty() {
        when(request.getOpLogs()).thenReturn(Collections.emptyMap());
        when(request.getNotebook()).thenReturn(Collections.emptyMap());
        when(request.getWarehouse()).thenReturn(Collections.emptyMap());
        when(request.getAudit()).thenReturn(Collections.emptyMap());

        FileSystem result = underTest.convert(request);

        assertTrue(result.getLocations().getMap().containsKey(LOCATIONS));
        assertTrue(((Collection<?>) result.getLocations().getMap().get(LOCATIONS)).isEmpty());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testConvertWhenSourceHasStorageLocationThenLocationJsonShouldBeNonempty() {
        Map<String, String> opLogs = Collections.singletonMap("oK", "oV");
        Map<String, String> notebook = Collections.singletonMap("nK", "nV");
        Map<String, String> warehouse = Collections.singletonMap("wK", "wV");
        Map<String, String> audit = Collections.singletonMap("aK", "aV");
        when(request.getOpLogs()).thenReturn(opLogs);
        when(request.getNotebook()).thenReturn(notebook);
        when(request.getWarehouse()).thenReturn(warehouse);
        when(request.getAudit()).thenReturn(audit);

        Answer<Void> answer = invocation -> {
            StorageLocation location = new StorageLocation();
            location.setCategory(invocation.getArgument(0));
            invocation.<Set<StorageLocation>>getArgument(2).add(location);
            return null;
        };
        doAnswer(answer).when(fileSystemConvertUtil).populateStorageLocationsFromMap(eq(StorageLocationCategory.OP_LOGS), eq(opLogs), any(Set.class));
        doAnswer(answer).when(fileSystemConvertUtil).populateStorageLocationsFromMap(eq(StorageLocationCategory.NOTEBOOK), eq(notebook), any(Set.class));
        doAnswer(answer).when(fileSystemConvertUtil).populateStorageLocationsFromMap(eq(StorageLocationCategory.WAREHOUSE), eq(warehouse), any(Set.class));
        doAnswer(answer).when(fileSystemConvertUtil).populateStorageLocationsFromMap(eq(StorageLocationCategory.AUDIT), eq(audit), any(Set.class));

        FileSystem result = underTest.convert(request);

        assertTrue(result.getLocations().getMap().containsKey(LOCATIONS));
        assertEquals(4L, ((Collection<?>) result.getLocations().getMap().get(LOCATIONS)).size());
    }

}
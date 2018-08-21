package com.sequenceiq.cloudbreak.converter.stack.cluster;

import static org.junit.Assert.assertNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.convert.ConversionService;

import com.sequenceiq.cloudbreak.api.model.KerberosRequest;
import com.sequenceiq.cloudbreak.api.model.stack.cluster.ClusterRequest;
import com.sequenceiq.cloudbreak.converter.AbstractJsonConverterTest;
import com.sequenceiq.cloudbreak.domain.FileSystem;
import com.sequenceiq.cloudbreak.domain.KerberosConfig;
import com.sequenceiq.cloudbreak.domain.organization.Organization;
import com.sequenceiq.cloudbreak.domain.stack.cluster.Cluster;
import com.sequenceiq.cloudbreak.domain.stack.cluster.gateway.Gateway;
import com.sequenceiq.cloudbreak.service.filesystem.FileSystemConfigService;
import com.sequenceiq.cloudbreak.service.organization.OrganizationService;

public class ClusterRequestToClusterConverterTest extends AbstractJsonConverterTest<ClusterRequest> {

    private static final long ORG_ID = 100L;

    @InjectMocks
    private ClusterRequestToClusterConverter underTest;

    @Mock
    private ConversionService conversionService;

    @Mock
    private FileSystemConfigService fileSystemConfigService;

    @Mock
    private OrganizationService organizationService;

    @Before
    public void setUp() {
        underTest = new ClusterRequestToClusterConverter();
        MockitoAnnotations.initMocks(this);
        Organization organization = new Organization();
        organization.setId(ORG_ID);
        when(organizationService.getDefaultOrganizationForCurrentUser()).thenReturn(organization);
    }

    @Test
    public void testConvert() {
        // GIVEN
        given(conversionService.convert(any(ClusterRequest.class), eq(Gateway.class))).willReturn(new Gateway());
        // WHEN
        Cluster result = underTest.convert(getRequest("cluster.json"));
        // THEN
        assertAllFieldsNotNull(result, Arrays.asList("stack", "blueprint", "creationStarted", "creationFinished", "upSince", "statusReason", "ambariIp",
                "ambariStackDetails", "fileSystem", "certDir", "rdsConfigs", "ldapConfig", "attributes", "blueprintCustomProperties", "uptime",
                "kerberosConfig", "ambariSecurityMasterKey", "proxyConfig", "extendedBlueprintText"));
    }

    @Test
    public void testConvertWithFileSystemDetails() {
        // GIVEN
        Gateway gateway = new Gateway();
        given(conversionService.convert(any(ClusterRequest.class), eq(Gateway.class))).willReturn(gateway);
        given(conversionService.convert(any(KerberosRequest.class), eq(KerberosConfig.class))).willReturn(new KerberosConfig());
        given(fileSystemConfigService.getByNameForOrganizationId("teszt", ORG_ID)).willReturn(new FileSystem());
        // WHEN
        Cluster result = underTest.convert(getRequest("cluster-with-file-system.json"));
        // THEN
        assertAllFieldsNotNull(result, Arrays.asList("stack", "blueprint", "creationStarted", "creationFinished", "upSince", "statusReason", "ambariIp",
                "ambariStackDetails", "certDir", "rdsConfigs", "ldapConfig", "attributes", "blueprintCustomProperties", "uptime",
                "ambariSecurityMasterKey", "proxyConfig", "extendedBlueprintText"));
    }

    @Test
    public void testNoGateway() {
        // GIVEN
        // WHEN
        ClusterRequest clusterRequest = getRequest("cluster-no-gateway.json");
        Cluster result = underTest.convert(clusterRequest);
        // THEN
        assertAllFieldsNotNull(result, Arrays.asList("stack", "blueprint", "creationStarted", "creationFinished", "upSince", "statusReason", "ambariIp",
                "ambariStackDetails", "fileSystem", "certDir", "rdsConfigs", "ldapConfig", "attributes", "blueprintCustomProperties", "uptime",
                "kerberosConfig", "ambariSecurityMasterKey", "proxyConfig", "extendedBlueprintText", "gateway"));
        assertNull(result.getGateway());
    }

    @Override
    public Class<ClusterRequest> getRequestClass() {
        return ClusterRequest.class;
    }
}

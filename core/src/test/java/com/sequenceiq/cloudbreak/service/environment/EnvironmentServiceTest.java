package com.sequenceiq.cloudbreak.service.environment;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.convert.ConversionService;

import com.sequenceiq.cloudbreak.api.model.CredentialRequest;
import com.sequenceiq.cloudbreak.api.model.environment.request.EnvironmentRequest;
import com.sequenceiq.cloudbreak.api.model.environment.response.DetailedEnvironmentResponse;
import com.sequenceiq.cloudbreak.common.model.user.CloudbreakUser;
import com.sequenceiq.cloudbreak.controller.validation.ValidationResult;
import com.sequenceiq.cloudbreak.controller.validation.environment.EnvironmentCreationValidator;
import com.sequenceiq.cloudbreak.domain.Credential;
import com.sequenceiq.cloudbreak.domain.environment.Environment;
import com.sequenceiq.cloudbreak.domain.workspace.User;
import com.sequenceiq.cloudbreak.domain.workspace.Workspace;
import com.sequenceiq.cloudbreak.repository.environment.EnvironmentRepository;
import com.sequenceiq.cloudbreak.service.RestRequestThreadLocalService;
import com.sequenceiq.cloudbreak.service.credential.CredentialService;
import com.sequenceiq.cloudbreak.service.ldapconfig.LdapConfigService;
import com.sequenceiq.cloudbreak.service.proxy.ProxyConfigService;
import com.sequenceiq.cloudbreak.service.rdsconfig.RdsConfigService;
import com.sequenceiq.cloudbreak.service.user.UserService;
import com.sequenceiq.cloudbreak.service.workspace.WorkspaceService;

@RunWith(MockitoJUnitRunner.class)
public class EnvironmentServiceTest {

    private static final Long WORKSPACE_ID = 1L;

    private static final String CREDENTIAL_NAME = "cred1";

    private static final String ENVIRONMENT_NAME = "EnvName";

    @Mock
    private UserService userService;

    @Mock
    private WorkspaceService workspaceService;

    @Mock
    private RestRequestThreadLocalService restRequestThreadLocalService;

    @Mock
    private RdsConfigService rdsConfigService;

    @Mock
    private LdapConfigService ldapConfigService;

    @Mock
    private ProxyConfigService proxyConfigService;

    @Mock
    private CredentialService credentialService;

    @Mock
    private EnvironmentCreationValidator environmentCreationValidator;

    @Mock
    private ConversionService conversionService;

    @Mock
    private EnvironmentRepository environmentRepository;

    @InjectMocks
    private EnvironmentService environmentService;

    private final Workspace workspace = new Workspace();

    @Before
    public void setup() {
        workspace.setId(WORKSPACE_ID);
        when(conversionService.convert(any(EnvironmentRequest.class), eq(Environment.class))).thenReturn(new Environment());
        when(ldapConfigService.findByNamesInWorkspace(anySet(), anyLong())).thenReturn(Collections.emptySet());
        when(rdsConfigService.findByNamesInWorkspace(anySet(), anyLong())).thenReturn(Collections.emptySet());
        when(proxyConfigService.findByNamesInWorkspace(anySet(), anyLong())).thenReturn(Collections.emptySet());
        when(environmentCreationValidator.validate(any())).thenReturn(ValidationResult.builder().build());
        when(workspaceService.get(anyLong(), any())).thenReturn(workspace);
        when(restRequestThreadLocalService.getCloudbreakUser()).thenReturn(new CloudbreakUser("", "", ""));
        when(userService.getOrCreate(any())).thenReturn(new User());
        when(conversionService.convert(any(Environment.class), eq(DetailedEnvironmentResponse.class))).thenReturn(new DetailedEnvironmentResponse());
        when(workspaceService.get(anyLong(), any())).thenReturn(workspace);
        when(workspaceService.retrieveForUser(any())).thenReturn(Set.of(workspace));
        when(environmentRepository.save(any(Environment.class))).thenReturn(new Environment());
    }

    @Test
    public void testCreateWithCredentialName() {
        EnvironmentRequest environmentRequest = new EnvironmentRequest();
        environmentRequest.setName(ENVIRONMENT_NAME);

        environmentRequest.setCredentialName(CREDENTIAL_NAME);
        CredentialRequest credentialRequest = new CredentialRequest();
        credentialRequest.setName("IgnoredCredRequestName");
        environmentRequest.setCredential(credentialRequest);

        when(credentialService.getByNameForWorkspaceId(CREDENTIAL_NAME, WORKSPACE_ID)).thenReturn(new Credential());

        DetailedEnvironmentResponse response = environmentService.createForLoggedInUser(environmentRequest, WORKSPACE_ID);

        assertNotNull(response);
    }

    @Test
    public void testCreateWithCredential() {
        EnvironmentRequest environmentRequest = new EnvironmentRequest();
        environmentRequest.setName(ENVIRONMENT_NAME);

        CredentialRequest credentialRequest = new CredentialRequest();
        credentialRequest.setName("CredRequestName");
        environmentRequest.setCredential(credentialRequest);

        when(credentialService.createForLoggedInUser(any(), anyLong())).thenReturn(new Credential());
        when(conversionService.convert(any(CredentialRequest.class), eq(Credential.class))).thenReturn(new Credential());

        DetailedEnvironmentResponse response = environmentService.createForLoggedInUser(environmentRequest, WORKSPACE_ID);

        assertNotNull(response);
    }
}
package com.sequenceiq.cloudbreak.security;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.ui.freemarker.FreeMarkerConfigurationFactoryBean;

import com.sequenceiq.cloudbreak.aspect.CheckPermissionsAspects;
import com.sequenceiq.cloudbreak.aspect.HasPermissionService;
import com.sequenceiq.cloudbreak.authorization.DisabledPermissionChecker;
import com.sequenceiq.cloudbreak.authorization.OrganizationIdPermissionChecker;
import com.sequenceiq.cloudbreak.authorization.OrganizationPermissionChecker;
import com.sequenceiq.cloudbreak.authorization.PermissionCheckerService;
import com.sequenceiq.cloudbreak.authorization.PermissionCheckingUtils;
import com.sequenceiq.cloudbreak.authorization.ReturnValuePermissionChecker;
import com.sequenceiq.cloudbreak.authorization.TargetIdPermissionChecker;
import com.sequenceiq.cloudbreak.authorization.TargetPermissionChecker;
import com.sequenceiq.cloudbreak.blueprint.validation.BlueprintValidator;
import com.sequenceiq.cloudbreak.common.model.user.IdentityUser;
import com.sequenceiq.cloudbreak.common.service.user.UserFilterField;
import com.sequenceiq.cloudbreak.conf.ConversionConfig;
import com.sequenceiq.cloudbreak.conf.SecurityConfig;
import com.sequenceiq.cloudbreak.controller.validation.network.NetworkConfigurationValidator;
import com.sequenceiq.cloudbreak.converter.CloudbreakConversionServiceFactoryBean;
import com.sequenceiq.cloudbreak.converter.scheduler.StatusToPollGroupConverter;
import com.sequenceiq.cloudbreak.core.bootstrap.service.container.ContainerOrchestratorResolver;
import com.sequenceiq.cloudbreak.core.flow2.service.ReactorFlowManager;
import com.sequenceiq.cloudbreak.domain.stack.Stack;
import com.sequenceiq.cloudbreak.repository.ComponentRepository;
import com.sequenceiq.cloudbreak.repository.InstanceGroupRepository;
import com.sequenceiq.cloudbreak.repository.InstanceMetaDataRepository;
import com.sequenceiq.cloudbreak.repository.OrchestratorRepository;
import com.sequenceiq.cloudbreak.repository.ResourceRepository;
import com.sequenceiq.cloudbreak.repository.SecurityConfigRepository;
import com.sequenceiq.cloudbreak.repository.StackRepository;
import com.sequenceiq.cloudbreak.repository.StackStatusRepository;
import com.sequenceiq.cloudbreak.repository.StackViewRepository;
import com.sequenceiq.cloudbreak.repository.organization.OrganizationRepository;
import com.sequenceiq.cloudbreak.repository.organization.TenantRepository;
import com.sequenceiq.cloudbreak.repository.organization.UserOrgPermissionsRepository;
import com.sequenceiq.cloudbreak.repository.organization.UserRepository;
import com.sequenceiq.cloudbreak.security.CheckPermissionAspectForMockitoTest.StubbingDeactivator;
import com.sequenceiq.cloudbreak.security.StackServiceSecurityComponentTest.TestConfig;
import com.sequenceiq.cloudbreak.service.AuthorizationService;
import com.sequenceiq.cloudbreak.service.Clock;
import com.sequenceiq.cloudbreak.service.ComponentConfigProvider;
import com.sequenceiq.cloudbreak.service.RepositoryLookupService;
import com.sequenceiq.cloudbreak.service.RestRequestThreadLocalService;
import com.sequenceiq.cloudbreak.service.StackUpdater;
import com.sequenceiq.cloudbreak.service.TlsSecurityService;
import com.sequenceiq.cloudbreak.service.TransactionService;
import com.sequenceiq.cloudbreak.service.cluster.ClusterService;
import com.sequenceiq.cloudbreak.service.credential.OpenSshPublicKeyValidator;
import com.sequenceiq.cloudbreak.service.decorator.StackResponseDecorator;
import com.sequenceiq.cloudbreak.service.events.CloudbreakEventService;
import com.sequenceiq.cloudbreak.service.image.ImageService;
import com.sequenceiq.cloudbreak.service.image.UserDataBuilder;
import com.sequenceiq.cloudbreak.service.image.UserDataBuilderParams;
import com.sequenceiq.cloudbreak.service.messages.CloudbreakMessagesService;
import com.sequenceiq.cloudbreak.service.organization.OrganizationModificationVerifierService;
import com.sequenceiq.cloudbreak.service.organization.OrganizationService;
import com.sequenceiq.cloudbreak.service.security.OwnerBasedPermissionEvaluator;
import com.sequenceiq.cloudbreak.service.security.ScimAccountGroupReaderFilter;
import com.sequenceiq.cloudbreak.service.stack.StackDownscaleValidatorService;
import com.sequenceiq.cloudbreak.service.stack.StackService;
import com.sequenceiq.cloudbreak.service.stack.connector.adapter.ServiceProviderConnectorAdapter;
import com.sequenceiq.cloudbreak.service.user.CachedUserDetailsService;
import com.sequenceiq.cloudbreak.service.user.UserOrgPermissionsService;
import com.sequenceiq.cloudbreak.service.user.UserOrgPermissionsValidator;
import com.sequenceiq.cloudbreak.service.user.UserService;

@SpringBootTest(classes = TestConfig.class)
@RunWith(SpringRunner.class)
@TestPropertySource(properties = {
        "profile=dev"
})
@MockBean({StackService.class, UserService.class, StackUpdater.class, ImageService.class, ClusterService.class, TlsSecurityService.class,
        ReactorFlowManager.class, BlueprintValidator.class, NetworkConfigurationValidator.class, CloudbreakEventService.class, CloudbreakMessagesService.class,
        ServiceProviderConnectorAdapter.class, ContainerOrchestratorResolver.class, ComponentConfigProvider.class, StackResponseDecorator.class,
        OpenSshPublicKeyValidator.class, StatusToPollGroupConverter.class, CloudbreakConversionServiceFactoryBean.class, ConversionConfig.class,
        UserDataBuilderParams.class, freemarker.template.Configuration.class, UserDataBuilder.class, ComponentConfigProvider.class})
public class StackServiceSecurityComponentTest {

    private static final String ACCOUNT_A = "accountA";

    private static final String ACCOUNT_B = "accountB";

    private static final String USER_A_ID = "userA";

    private static final String USER_B_ID = "userBId";

    private static final String PERMISSION_READ = "READ";

    private static final String PERMISSION_WRITE = "WRITE";

    private static final long ORGANIZATION_1_ID = 1L;

    @Inject
    private StackRepository stackRepository;

    @Inject
    private StackViewRepository stackViewRepository;

    @Inject
    private StackStatusRepository stackStatusRepository;

    @Inject
    private CheckPermissionAspectForMockitoTest checkPermissionAspectForMockitoTest;

    @Inject
    private StackService underTest;

    @Inject
    private ApplicationContext applicationContext;

    @Inject
    private CachedUserDetailsService cachedUserDetailsService;

    @Test
    public void testApplicationContext(){
        assertNotNull(applicationContext);
    }

    @Test(expected = AccessDeniedException.class)
    public void testFindByNameAndOrganizationIdWithLists() throws Exception {
        when(stackRepository.findByNameAndOrganizationIdWithLists(anyString(), anyLong())).thenReturn(getAStack());
        IdentityUser loggedInUser = getUserFromDifferentAccount(true);
        setupLoggedInUser(loggedInUser);

        try (StubbingDeactivator deactivator = checkPermissionAspectForMockitoTest.new StubbingDeactivator()) {
            underTest.findStackByNameAndOrganizationId(USER_A_ID, ORGANIZATION_1_ID);

        } catch (TransactionService.TransactionRuntimeExecutionException e) {
            throw getRootCauseOfTransactionException(e);
        } finally {
//            verify(ownerBasedPermissionEvaluator).hasPermission(any(), eq(foundStacks), eq(PERMISSION_READ));
        }
    }

    protected IdentityUser getUserFromDifferentAccount(boolean admin, String... scopes) {
//        addScopes(scopes);
//        return new IdentityUser(USER_B_ID, "", ACCOUNT_B, getIdentityUserRoles(admin), "", "", new Date());
        return null;
    }

    protected void setupLoggedInUser(IdentityUser loggedInUser) {
        when(cachedUserDetailsService.getDetails(anyString(), any(UserFilterField.class))).thenReturn(loggedInUser);
    }

    private Exception getRootCauseOfTransactionException(TransactionService.TransactionRuntimeExecutionException e) {
        return (Exception) e.getCause().getCause();
    }

    private Stack getAStack() {
        Stack stack = new Stack();
        stack.setOwner(USER_A_ID);
        stack.setAccount(ACCOUNT_A);
        return stack;
    }

    @Configuration
    @EnableAspectJAutoProxy(proxyTargetClass = true)
    @ComponentScan(basePackages =
            {"com.sequenceiq.cloudbreak"},
            useDefaultFilters = false,
            includeFilters = {
                    @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = {
                            StackService.class,

                            UserOrgPermissionsService.class,
                            UserService.class,
                            PermissionCheckerService.class,
                            StackService.class,
                            TransactionService.class,
                            Clock.class,
                            StackDownscaleValidatorService.class,
                            UserService.class,
                            DisabledPermissionChecker.class,
                            OrganizationIdPermissionChecker.class,
                            OrganizationPermissionChecker.class,
                            ReturnValuePermissionChecker.class,
                            TargetIdPermissionChecker.class,
                            TargetPermissionChecker.class,
                            PermissionCheckingUtils.class,
                            RestRequestThreadLocalService.class,
                            UserOrgPermissionsValidator.class,
                            OrganizationService.class,
                            OrganizationModificationVerifierService.class,

                            // old, owner based permission checking, to be deleted
                            OwnerBasedPermissionEvaluator.class,
                            ScimAccountGroupReaderFilter.class,
                            SecurityConfig.class,
                            HasPermissionService.class,
                            AuthorizationService.class
                    })
            })
    public static class TestConfig {

        @MockBean
        private CachedUserDetailsService cachedUserDetailsService;

        @MockBean
        private OrganizationRepository organizationRepository;

        @MockBean
        private StackViewRepository stackViewRepository;

        @MockBean
        private StackStatusRepository stackStatusRepository;

        @MockBean
        private InstanceMetaDataRepository instanceMetaDataRepository;

        @MockBean
        private InstanceGroupRepository instanceGroupRepository;

        @MockBean
        private OrchestratorRepository orchestratorRepository;

        @MockBean
        private SecurityConfigRepository securityConfigRepository;

        @MockBean
        private UserOrgPermissionsRepository userOrgPermissionsRepository;

        @MockBean
        private UserRepository userRepository;

        @MockBean
        private TenantRepository tenantRepository;

        @MockBean
        private ResourceRepository resourceRepository;

        @MockBean
        private ComponentRepository componentRepository;

        @MockBean
        private ResourceServerTokenServices resourceServerTokenServices;

        @MockBean
        private RepositoryLookupService repositoryLookupService;

        @Bean
        public CheckPermissionsAspects checkPermissionsAspects() {
            return new CheckPermissionsAspects();
        }

        @Bean
        public HasPermissionService hasPermissionService() {
            return new HasPermissionServiceForMockitoTest();
        }

        @Bean
        public StackRepository stackRepository(){
            return mock(StackRepository.class);
        }

        @Bean
        public FreeMarkerConfigurationFactoryBean freeMarkerConfigurationFactoryBean(){
            return new FreeMarkerConfigurationFactoryBean();
        }

    }
}
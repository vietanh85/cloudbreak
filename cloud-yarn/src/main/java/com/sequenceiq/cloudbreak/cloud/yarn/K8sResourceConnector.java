package com.sequenceiq.cloudbreak.cloud.yarn;

import static com.sequenceiq.cloudbreak.common.type.ResourceType.YARN_APPLICATION;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import com.sequenceiq.cloudbreak.api.model.AdjustmentType;
import com.sequenceiq.cloudbreak.cloud.PlatformParametersConsts;
import com.sequenceiq.cloudbreak.cloud.ResourceConnector;
import com.sequenceiq.cloudbreak.cloud.context.AuthenticatedContext;
import com.sequenceiq.cloudbreak.cloud.exception.CloudConnectorException;
import com.sequenceiq.cloudbreak.cloud.exception.CloudOperationNotSupportedException;
import com.sequenceiq.cloudbreak.cloud.exception.TemplatingDoesNotSupportedException;
import com.sequenceiq.cloudbreak.cloud.model.CloudInstance;
import com.sequenceiq.cloudbreak.cloud.model.CloudResource;
import com.sequenceiq.cloudbreak.cloud.model.CloudResource.Builder;
import com.sequenceiq.cloudbreak.cloud.model.CloudResourceStatus;
import com.sequenceiq.cloudbreak.cloud.model.CloudStack;
import com.sequenceiq.cloudbreak.cloud.model.Group;
import com.sequenceiq.cloudbreak.cloud.model.InstanceTemplate;
import com.sequenceiq.cloudbreak.cloud.model.ResourceStatus;
import com.sequenceiq.cloudbreak.cloud.model.TlsInfo;
import com.sequenceiq.cloudbreak.cloud.notification.PersistenceNotifier;
import com.sequenceiq.cloudbreak.cloud.yarn.client.model.core.Artifact;
import com.sequenceiq.cloudbreak.cloud.yarn.client.model.core.ConfigFile;
import com.sequenceiq.cloudbreak.cloud.yarn.client.model.core.ConfigFileType;
import com.sequenceiq.cloudbreak.cloud.yarn.client.model.core.Configuration;
import com.sequenceiq.cloudbreak.cloud.yarn.client.model.core.Resource;
import com.sequenceiq.cloudbreak.cloud.yarn.client.model.core.YarnComponent;
import com.sequenceiq.cloudbreak.cloud.yarn.client.model.request.CreateApplicationRequest;

@Service
public class K8sResourceConnector implements ResourceConnector<Object> {

    private static final Logger LOGGER = LoggerFactory.getLogger(K8sResourceConnector.class);

    private static final String ARTIFACT_TYPE_DOCKER = "DOCKER";

    private static final int APP_NAME_LENGTH = 128;

    @Override
    public List<CloudResourceStatus> launch(AuthenticatedContext authenticatedContext, CloudStack stack, PersistenceNotifier persistenceNotifier,
            AdjustmentType adjustmentType, Long threshold) throws Exception {
        String applicationName = createApplicationName(authenticatedContext);

        createApplication(stack, applicationName);

        CloudResource yarnApplication = new Builder().type(YARN_APPLICATION).name(applicationName).build();
        persistenceNotifier.notifyAllocation(yarnApplication, authenticatedContext.getCloudContext());
        return check(authenticatedContext, Collections.singletonList(yarnApplication));
    }

    private CreateApplicationRequest createApplication(CloudStack stack, String applicationName)
            throws Exception {
        CreateApplicationRequest createApplicationRequest = new CreateApplicationRequest();
        createApplicationRequest.setName(applicationName);

        Artifact artifact = new Artifact();
        artifact.setId(stack.getImage().getImageName());
        artifact.setType(ARTIFACT_TYPE_DOCKER);

        List<YarnComponent> components = stack.getGroups().stream()
                .map(group -> mapGroupToComponent(group, stack, artifact))
                .collect(Collectors.toList());

        createApplicationRequest.setComponents(components);
        int i = 0;
        for (YarnComponent c : components) {
            K8sApiUtils.createK8sApp(applicationName, c);
        }
        return createApplicationRequest;
    }

    private YarnComponent mapGroupToComponent(Group group, CloudStack stack, Artifact artifact) {
        return mapGroupToK8sComponent(group, stack, artifact);
    }

    private YarnComponent mapGroupToK8sComponent(Group group, CloudStack stack, Artifact artifact) {
        YarnComponent component = new YarnComponent();
        component.setName(group.getName());
        component.setNumberOfContainers(group.getInstancesSize());
        String userData = stack.getImage().getUserDataByType(group.getType());
        component.setLaunchCommand(String.format("/bootstrap/start-systemd '%s' '%s' '%s'", Base64.getEncoder().encodeToString(userData.getBytes()),
                stack.getLoginUserName(), stack.getPublicKey()));
        component.setArtifact(artifact);
        component.setDependencies(new ArrayList<>());
        InstanceTemplate instanceTemplate = group.getReferenceInstanceConfiguration().getTemplate();
        Resource resource = new Resource();
        resource.setCpus(instanceTemplate.getParameter(PlatformParametersConsts.CUSTOM_INSTANCETYPE_CPUS, Integer.class));
        resource.setMemory(instanceTemplate.getParameter(PlatformParametersConsts.CUSTOM_INSTANCETYPE_MEMORY, Integer.class));
        component.setResource(resource);
        component.setRunPrivilegedContainer(true);

        Configuration configuration = new Configuration();
        Map<String, String> propsMap = Maps.newHashMap();
        propsMap.put("userData", '\'' + Base64.getEncoder().encodeToString(userData.getBytes()) + '\'');
        propsMap.put("sshUser", '\'' + stack.getLoginUserName() + '\'');
        propsMap.put("groupname", '\'' + group.getName() + '\'');
        propsMap.put("sshPubKey", '\'' + stack.getPublicKey() + '\'');
        configuration.setProperties(propsMap);
        ConfigFile configFileProps = new ConfigFile();
        configFileProps.setType(ConfigFileType.PROPERTIES.name());
        configFileProps.setSrcFile("cb-conf");
        configFileProps.setDestFile("/etc/cloudbreak-config.props");
        configuration.setFiles(Collections.singletonList(configFileProps));

        component.setConfiguration(configuration);
        return component;
    }

    @Override
    public List<CloudResourceStatus> check(AuthenticatedContext authenticatedContext, List<CloudResource> resources) {
        List<CloudResourceStatus> result = new ArrayList<>();
        for (CloudResource resource : resources) {
            switch (resource.getType()) {
                case YARN_APPLICATION:
                    result.add(new CloudResourceStatus(resource, ResourceStatus.CREATED));
                    break;
                default:
                    throw new CloudConnectorException(String.format("Invalid resource type: %s", resource.getType()));
            }
        }
        return result;
    }

    @Override
    public List<CloudResourceStatus> terminate(AuthenticatedContext authenticatedContext,
            CloudStack stack, List<CloudResource> cloudResources) throws Exception {
        List<CloudResourceStatus> result = new ArrayList<>();
        for (CloudResource resource : cloudResources) {
            switch (resource.getType()) {
                case YARN_APPLICATION:
                    K8sApiUtils.deleteK8sApp(resource.getName());
                    result.add(new CloudResourceStatus(resource, ResourceStatus.DELETED));
                    break;
                default:
                    throw new CloudConnectorException(String.format("Invalid resource type: %s", resource.getType()));
            }
        }
        return result;
    }

    @Override
    public List<CloudResourceStatus> update(AuthenticatedContext authenticatedContext, CloudStack stack, List<CloudResource> resources) {
        return null;
    }

    @Override
    public List<CloudResourceStatus> upscale(AuthenticatedContext authenticatedContext, CloudStack stack, List<CloudResource> resources) {
        throw new CloudOperationNotSupportedException("Upscale stack operation is not supported on YARN");
    }

    @Override
    public List<CloudResourceStatus> downscale(AuthenticatedContext authenticatedContext, CloudStack stack, List<CloudResource> resources,
            List<CloudInstance> vms, Object resourcesToRemove) {
        throw new CloudOperationNotSupportedException("Downscale stack operation is not supported on YARN");
    }

    @Override
    public Object collectResourcesToRemove(AuthenticatedContext authenticatedContext, CloudStack stack, List<CloudResource> resources, List<CloudInstance> vms) {
        throw new CloudOperationNotSupportedException("Downscale resources collection operation is not supported on YARN");
    }

    @Override
    public TlsInfo getTlsInfo(AuthenticatedContext authenticatedContext, CloudStack cloudStack) {
        return new TlsInfo(false);
    }

    @Override
    public String getStackTemplate() throws TemplatingDoesNotSupportedException {
        throw new TemplatingDoesNotSupportedException();
    }

    private String createApplicationName(AuthenticatedContext ac) {
        return String.format("%s-%s", Splitter.fixedLength(APP_NAME_LENGTH - (ac.getCloudContext().getId().toString().length() + 1))
                .splitToList(ac.getCloudContext().getName()).get(0), ac.getCloudContext().getId());
    }
}

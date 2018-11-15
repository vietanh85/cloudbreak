package com.sequenceiq.cloudbreak.service.stack.flow;

import static com.sequenceiq.cloudbreak.common.type.CloudConstants.AWS;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.sequenceiq.cloudbreak.api.model.stack.instance.InstanceMetadataType;
import com.sequenceiq.cloudbreak.cloud.model.CloudResource;
import com.sequenceiq.cloudbreak.cloud.model.CloudResourceStatus;
import com.sequenceiq.cloudbreak.cloud.model.VolumeSetAttributes;
import com.sequenceiq.cloudbreak.common.type.ResourceType;
import com.sequenceiq.cloudbreak.domain.stack.Stack;
import com.sequenceiq.cloudbreak.domain.stack.instance.InstanceMetaData;
import com.sequenceiq.cloudbreak.orchestrator.exception.CloudbreakOrchestratorFailedException;
import com.sequenceiq.cloudbreak.orchestrator.model.GatewayConfig;
import com.sequenceiq.cloudbreak.orchestrator.salt.SaltOrchestrator;
import com.sequenceiq.cloudbreak.service.CloudbreakServiceException;
import com.sequenceiq.cloudbreak.service.GatewayConfigService;
import com.sequenceiq.cloudbreak.service.eventbus.CloudResourcePersisterService;
import com.sequenceiq.cloudbreak.service.resource.ResourceService;
import com.sequenceiq.cloudbreak.service.stack.StackService;

@Service
public class VolumeMetadataSetup {

    private static final Logger LOGGER = LoggerFactory.getLogger(VolumeMetadataSetup.class);
    public static final String NO_RESPONSE = "false";

    @Inject
    private StackService stackService;

    @Inject
    private SaltOrchestrator saltOrchestrator;

    @Inject
    private GatewayConfigService gatewayConfigService;

    @Inject
    private ResourceService resourceService;

    @Inject
    private CloudResourcePersisterService cloudResourcePersisterService;

    public void setupVolumeMetadata(Long stackId) {
        Stack stack = stackService.getByIdWithListsInTransaction(stackId);
        switch (stack.getPlatformVariant()) {
            case AWS:
                setupAwsVolumeMetadata(stack);
        }
    }

    private void setupAwsVolumeMetadata(Stack stack) {
        Boolean enableKnox = stack.getCluster().getGateway() != null;
        GatewayConfig gatewayConfig = getGatewayConfig(stack, enableKnox);
        try {
            Map<String, String> fstabHostsMap = saltOrchestrator.runCommandOnAllHosts(gatewayConfig, "cat /etc/fstab");
            List<String> notRespondingNodes = fstabHostsMap.entrySet().stream()
                    .filter(e -> NO_RESPONSE.equals(e.getValue()))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
            if(!notRespondingNodes.isEmpty()){
                throw new CloudbreakServiceException("Could not gather volume metadata, because some nodes did not respond: " + String.join(",", notRespondingNodes));
            }

            List<CloudResourceStatus> volumeSets = resourceService.getAllByTypeAsCloudResourceStatus(stack.getId(), ResourceType.AWS_VOLUMESET);
            new StackVolumeSets(volumeSets, stack)
                    .setFstab(fstabHostsMap)
                    .getAll()
                    .forEach(stackVolumeSet -> cloudResourcePersisterService.update(stackVolumeSet.getCloudResource(), stack.getId()));
        } catch (CloudbreakOrchestratorFailedException e) {
            LOGGER.error("Could not gather metadata of volumes", e);
            throw new CloudbreakServiceException("Volume metadata collection failed", e);
        }
    }

    private static class StackVolumeSets {
        private static final Logger LOGGER = LoggerFactory.getLogger(StackVolumeSets.class);

        private final Map<String, CloudResourceStatus> fqdnToVolumeSetAttributesMap = new HashMap<>();

        StackVolumeSets(List<CloudResourceStatus> cloudResourceStatusList, Stack stack) {
            Map<String, String> instanceIdToFqdnMap = prepareInstanceIdToFqdnMap(stack);
            prepareFqdnToVolumeSetAttributeMap(cloudResourceStatusList, instanceIdToFqdnMap);
        }

        StackVolumeSets setFstab(Map<String, String> fstabOfRespondingHostsMap){
            fstabOfRespondingHostsMap.forEach(this::setFstab);
            return this;
        }

        private void setFstab(String fqdn, String fstab) {
            CloudResourceStatus cloudResourceStatus = fqdnToVolumeSetAttributesMap.get(fqdn);
            if (cloudResourceStatus == null) {
                LOGGER.info("CloudResourceStatus not found. Fqdn: {}", fqdn);
                return;
            }
            VolumeSetAttributes volumeSetAttributes = getVolumeSetAttributes(cloudResourceStatus);
            if (volumeSetAttributes == null) {
                LOGGER.info("VolumeSetAttributes not found. Fqdn: {}", fqdn);
                return;
            }
            volumeSetAttributes.setFstab(fstab);
        }

        public Collection<CloudResourceStatus> getAll() {
            return fqdnToVolumeSetAttributesMap.values();
        }

        private void prepareFqdnToVolumeSetAttributeMap(List<CloudResourceStatus> cloudResourceStatusList, Map<String, String> instanceIdToFqdnMap) {
            cloudResourceStatusList.forEach(cloudResourceStatus -> {
                String instanceId = cloudResourceStatus.getCloudResource().getInstanceId();
                String fqdn = instanceIdToFqdnMap.get(instanceId);
                if (fqdn == null) {
                    return;
                }
                fqdnToVolumeSetAttributesMap.put(fqdn, cloudResourceStatus);
            });
        }

        private Map<String, String> prepareInstanceIdToFqdnMap(Stack stack) {
            Map<String, String> fqdnToInstanceIdMap = new HashMap<>();
            stack.getInstanceGroups().stream()
                    .flatMap(instanceGroup -> instanceGroup.getAllInstanceMetaData().stream())
                    .forEach(instanceMetaData -> fqdnToInstanceIdMap.put(instanceMetaData.getInstanceId(), instanceMetaData.getDiscoveryFQDN()));
            return fqdnToInstanceIdMap;
        }

        private VolumeSetAttributes getVolumeSetAttributes(CloudResourceStatus cloudResourceStatus) {
            return cloudResourceStatus.getCloudResource().getParameter(CloudResource.ATTRIBUTES, VolumeSetAttributes.class);
        }
    }

    public GatewayConfig getGatewayConfig(Stack stack, Boolean enableKnox) {
        GatewayConfig gatewayConfig = null;
        for (InstanceMetaData gateway : stack.getGatewayInstanceMetadata()) {
            if (InstanceMetadataType.GATEWAY_PRIMARY.equals(gateway.getInstanceMetadataType())) {
                gatewayConfig = gatewayConfigService.getGatewayConfig(stack, gateway, enableKnox);
            }
        }
        return gatewayConfig;
    }
}

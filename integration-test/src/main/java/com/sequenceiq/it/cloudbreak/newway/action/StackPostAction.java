package com.sequenceiq.it.cloudbreak.newway.action;

import static com.sequenceiq.it.cloudbreak.newway.log.Log.log;
import static com.sequenceiq.it.cloudbreak.newway.log.Log.logJSON;
import static org.springframework.util.StringUtils.isEmpty;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sequenceiq.cloudbreak.api.model.v2.AmbariV2Request;
import com.sequenceiq.cloudbreak.api.model.v2.ClusterV2Request;
import com.sequenceiq.cloudbreak.api.model.v2.NetworkV2Request;
import com.sequenceiq.it.cloudbreak.newway.AccessConfig;
import com.sequenceiq.it.cloudbreak.newway.CloudbreakClient;
import com.sequenceiq.it.cloudbreak.newway.ClusterGateway;
import com.sequenceiq.it.cloudbreak.newway.CredentialEntity;
import com.sequenceiq.it.cloudbreak.newway.DatalakeCluster;
import com.sequenceiq.it.cloudbreak.newway.ImageSettings;
import com.sequenceiq.it.cloudbreak.newway.Kerberos;
import com.sequenceiq.it.cloudbreak.newway.StackEntity;
import com.sequenceiq.it.cloudbreak.newway.context.TestContext;
import com.sequenceiq.it.cloudbreak.newway.entity.AmbariEntity;
import com.sequenceiq.it.cloudbreak.newway.entity.ClusterEntity;
import com.sequenceiq.it.cloudbreak.newway.entity.InstanceGroupEntity;

public class StackPostAction implements ActionV2<StackEntity> {

    private static final Logger LOGGER = LoggerFactory.getLogger(StackPostAction.class);

    private static final String SUBNET_ID_KEY = "subnetId";

    private static final String NETWORK_ID_KEY = "networkId";

    @Override
    public StackEntity action(TestContext testContext, StackEntity entity, CloudbreakClient client) throws Exception {

        CredentialEntity credential = testContext.get(CredentialEntity.class);

        if (credential != null) {
            entity.getRequest().getGeneral().setCredentialName(credential.getName());
        }

        ClusterEntity cluster = testContext.get(ClusterEntity.class);
        if (cluster != null) {
            checkOldEntityExists(entity.getRequest().getCluster());
            entity.getRequest().setCluster(cluster.getRequest());

            AmbariEntity ambariEntity = testContext.get(AmbariEntity.class);
            if (ambariEntity != null) {
                checkOldEntityExists(entity.getRequest().getCluster().getAmbari());
                entity.getRequest().getCluster().setAmbari(ambariEntity.getRequest());
            }

            if (cluster.getRequest().getCloudStorage() != null && cluster.getRequest().getCloudStorage().getS3()
                    != null && isEmpty(cluster.getRequest().getCloudStorage().getS3().getInstanceProfile())) {
                AccessConfig accessConfig = testContext.get(AccessConfig.class);
                List<String> arns = accessConfig
                        .getResponse()
                        .getAccessConfigs()
                        .stream()
                        .map(accessConfigJson -> accessConfigJson.getProperties().get("arn").toString())
                        .sorted()
                        .distinct()
                        .collect(Collectors.toList());
                checkOldEntityExists(cluster.getRequest().getCloudStorage().getS3().getInstanceProfile());
                cluster.getRequest().getCloudStorage().getS3().setInstanceProfile(arns.get(0));
            } else if (cluster.getRequest().getCloudStorage() != null && cluster.getRequest().getCloudStorage().getGcs() != null && credential != null) {
                cluster.getRequest().getCloudStorage().getGcs()
                        .setServiceAccountEmail(credential.getResponse().getParameters().get("serviceAccountId").toString());
            }
        }

        Kerberos kerberos = testContext.get(Kerberos.class);
        boolean updateKerberos = entity.getRequest().getCluster() != null && entity.getRequest().getCluster().getAmbari() != null
                && entity.getRequest().getCluster().getAmbari().getKerberos() == null;
        if (kerberos != null && updateKerberos) {
            checkOldEntityExists(entity.getRequest().getCluster().getAmbari().getKerberos());
            AmbariV2Request ambariReq = entity.getRequest().getCluster().getAmbari();
            ambariReq.setEnableSecurity(true);
            ambariReq.setKerberos(kerberos.getRequest());
        }

        ClusterGateway clusterGateway = testContext.get(ClusterGateway.class);
        if (clusterGateway != null) {
            if (entity.hasCluster()) {
                ClusterV2Request clusterV2Request = entity.getRequest().getCluster();
                AmbariV2Request ambariV2Request = clusterV2Request.getAmbari();
                if (ambariV2Request != null) {
                    checkOldEntityExists(entity.getRequest().getCluster().getAmbari().getGateway());
                    ambariV2Request.setGateway(clusterGateway.getRequest());
                }
            }
        }

        ImageSettings imageSettings = testContext.get(ImageSettings.class);
        if (imageSettings != null) {
            checkOldEntityExists(entity.getRequest().getImageSettings());
            entity.getRequest().setImageSettings(imageSettings.getRequest());
        }

        InstanceGroupEntity hostGroups = testContext.get(InstanceGroupEntity.class);
        if (hostGroups != null) {
            checkOldEntityExists(entity.getRequest().getInstanceGroups());
            entity.getRequest().setInstanceGroups(Collections.singletonList(hostGroups.getRequest()));
        }

        var datalakeStack = testContext.get(DatalakeCluster.class);
        if (datalakeStack != null && datalakeStack.getResponse() != null && datalakeStack.getResponse().getNetwork() != null) {
            String subnetId = null;
            String networkId = null;
            var properties = Optional.ofNullable(datalakeStack.getResponse().getNetwork().getParameters());
            if (properties.isPresent()) {
                if (!isEmpty(properties.get().get(SUBNET_ID_KEY))) {
                    subnetId = properties.get().get(SUBNET_ID_KEY).toString();
                }
                if (!isEmpty(properties.get().get(NETWORK_ID_KEY))) {
                    networkId = properties.get().get(NETWORK_ID_KEY).toString();
                }
            }
            if (entity.getRequest().getNetwork() != null && entity.getRequest().getNetwork().getParameters() != null) {
                entity.getRequest().getNetwork().getParameters().put(SUBNET_ID_KEY, subnetId);
                entity.getRequest().getNetwork().getParameters().put(NETWORK_ID_KEY, networkId);
            } else {
                var network = new NetworkV2Request();
                var params = new LinkedHashMap<String, Object>();
                params.put(SUBNET_ID_KEY, subnetId);
                params.put(NETWORK_ID_KEY, networkId);
                network.setParameters(params);
                checkOldEntityExists(entity.getRequest().getNetwork());
                entity.getRequest().setNetwork(network);
            }
            entity.getRequest().getNetwork().setSubnetCIDR(null);
            entity.getRequest().getNetwork().getParameters().put("routerId", null);
            entity.getRequest().getNetwork().getParameters().put("publicNetId", null);
            entity.getRequest().getNetwork().getParameters().put("noPublicIp", false);
            entity.getRequest().getNetwork().getParameters().put("noFirewallRules", false);
            entity.getRequest().getNetwork().getParameters().put("internetGatewayId", null);
        }

        log(" Name:\n" + entity.getRequest().getGeneral().getName());
        logJSON(" Stack post request:\n", entity.getRequest());
        entity.setResponse(
                client.getCloudbreakClient()
                        .stackV3Endpoint()
                        .createInWorkspace(client.getWorkspaceId(), entity.getRequest()));
        logJSON(" Stack post response:\n", entity.getResponse());
        log(" ID:\n" + entity.getResponse().getId());

        return entity;
    }

    private void checkOldEntityExists(Object entity) {
        if (entity != null) {
            LOGGER.warn("{} udpated from test context.", entity.getClass().getSimpleName());
        }
    }
}

package com.sequenceiq.it.cloudbreak.newway.mock;

import static com.sequenceiq.it.cloudbreak.newway.Mock.gson;
import static com.sequenceiq.it.cloudbreak.newway.Mock.responseFromJsonFile;
import static com.sequenceiq.it.spark.ITResponse.AMBARI_API_ROOT;

import java.util.Map;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sequenceiq.cloudbreak.cloud.model.CloudVmMetaDataStatus;
import com.sequenceiq.it.spark.ITResponse;
import com.sequenceiq.it.spark.ambari.AmbariCheckResponse;
import com.sequenceiq.it.spark.ambari.AmbariClusterRequestsResponse;
import com.sequenceiq.it.spark.ambari.AmbariClusterResponse;
import com.sequenceiq.it.spark.ambari.AmbariClustersHostsResponseW;
import com.sequenceiq.it.spark.ambari.AmbariComponentStatusOnHostResponse;
import com.sequenceiq.it.spark.ambari.AmbariHostsResponse;
import com.sequenceiq.it.spark.ambari.AmbariServiceConfigResponse;
import com.sequenceiq.it.spark.ambari.AmbariServicesComponentsResponse;
import com.sequenceiq.it.spark.ambari.AmbariStatusResponse;
import com.sequenceiq.it.spark.ambari.AmbariVersionDefinitionResponse;
import com.sequenceiq.it.spark.ambari.AmbariViewResponse;
import com.sequenceiq.it.spark.ambari.EmptyAmbariClusterResponse;
import com.sequenceiq.it.spark.ambari.EmptyAmbariResponse;
import com.sequenceiq.it.spark.ambari.v2.AmbariCategorizedHostComponentStateResponse;
import com.sequenceiq.it.util.HostNameUtil;

import spark.Service;

public class AmbariMock extends AbstractModelMock {
    public static final String CLUSTERS_CLUSTER = "/clusters/:cluster";

    public static final String CLUSTERS_CLUSTER_NAME_CONFIGURATIONS_SERVICE_CONFIG_VERSIONS = CLUSTERS_CLUSTER + "/configurations/service_config_versions";

    public static final String CLUSTERS_CLUSTER_HOSTS = CLUSTERS_CLUSTER + "/hosts";

    public static final String CLUSTERS_CLUSTER_HOSTS_HOSTNAME_HOST_COMPONENTS = CLUSTERS_CLUSTER + "/hosts/:hostname/host_components/*";

    public static final String CLUSTERS_CLUSTER_HOSTS_INTERNALHOSTNAME = CLUSTERS_CLUSTER + "/hosts/:internalhostname";

    public static final String CLUSTERS_CLUSTER_HOST_COMPONENTS = CLUSTERS_CLUSTER + "/host_components";

    public static final String CLUSTERS_CLUSTER_HOSTS_HOSTNAME = CLUSTERS_CLUSTER + "/hosts/:hostname";

    public static final String CLUSTERS_CLUSTER_SERVICES = CLUSTERS_CLUSTER + "/services/*";

    public static final String CLUSTERS_CLUSTER_SERVICES_HDFS_COMPONENTS_NAMENODE = CLUSTERS_CLUSTER + "/services/HDFS/components/NAMENODE";

    public static final String CLUSTERS_CLUSTER_REQUESTS_REQUEST = CLUSTERS_CLUSTER + "/requests/:request";

    public static final String CLUSTERS_CLUSTER_REQUESTS = CLUSTERS_CLUSTER + "/requests";

    public static final String STACKS_HDP_VERSIONS_VERSION_OPERATING_SYSTEMS_OS_REPOSITORIES_HDPVERSION
            = "/stacks/HDP/versions/:version/operating_systems/:os/repositories/:hdpversion";

    public static final String USERS = "/users";

    public static final String USERS_ADMIN = "/users/admin";

    public static final String BLUEPRINTS = "/blueprints/*";

    public static final String BLUEPRINTS_BLUEPRINTNAME = "/blueprints/:blueprintname";

    public static final String SERVICES_AMBARI_COMPONENTS_AMBARI_SERVER = "/services/AMBARI/components/AMBARI_SERVER";

    public static final String VIEWS_VIEW_VERSIONS_1_0_0_INSTANCES = "/views/:view/versions/1.0.0/instances/*";

    public static final String CHECK = "/check";

    public static final String VIEWS = "/views/*";

    public AmbariMock(Service sparkService, Model model) {
        super(sparkService, model);
    }

    public void addAmbariMappingsOld() {
        getAmbariClusterRequest(getSparkService());
        getAmbariClusters(getModel().getClusterName(), getModel().getInstanceMap(), getSparkService());
        postAmbariClusterRequest(getSparkService());
        getAmbariCheck(getSparkService());
        postAmbariUsers(getSparkService());
        postAmbariCluster(getSparkService());
        getAmbariBlueprint(getSparkService());
        getAmbariClusterHosts(getModel().getInstanceMap(), getSparkService(), "STARTED");
        postAmbariInstances(getSparkService());
        postAmbariClusters(getSparkService());
        getAmbariComponents(getSparkService());
        getAmbariHosts(getModel().getInstanceMap(), getSparkService());
        postAmbariBlueprints(getSparkService());
        putAmbariUsersAdmin(getSparkService());
        getAmbariClusterHosts(getModel().getInstanceMap(), getSparkService());
        putAmbariHdpVersion(getSparkService());
        getAmabriVersionDefinitions(getSparkService());
        postAmbariVersionDefinitions(getSparkService());
    }

    public void addAmbariMappings() {
        Map<String, CloudVmMetaDataStatus> instanceMap = getModel().getInstanceMap();
        Service sparkService = getSparkService();

        getAmbariClusterRequest(sparkService);
        getAmbariClusters(getModel().getClusterName(), instanceMap, sparkService);
        postAmbariClusterRequest(sparkService);
        getAmbariCheck(sparkService);
        postAmbariUsers(sparkService);
        postAmbariCluster(sparkService);
        getAmbariBlueprint(sparkService);
        getAmbariClusterHosts(instanceMap, sparkService, "STARTED");
        getAmbariHosts(instanceMap, sparkService);
        postAmbariInstances(sparkService);
        postAmbariClusters(sparkService);
        getAmbariComponents(sparkService);
        postAmbariBlueprints(sparkService);
        putAmbariUsersAdmin(sparkService);
        getAmbariClusterHosts(instanceMap, sparkService);
        putAmbariHdpVersion(sparkService);
        getAmabriVersionDefinitions(sparkService);
        postAmbariVersionDefinitions(sparkService);

        getAmbariCluster(getModel().getClusterName(), instanceMap, sparkService);
        getAmbariClusterHosts(instanceMap, sparkService, "INSTALLED");
        putAmbariClusterServices(sparkService);
        postAmbariClusterHosts(sparkService);
        getAmbariClusterHostComponents(sparkService);
        getAmbariClusterConfigurationVersions(sparkService);
        getAmbariClusterHostStatus(sparkService);
        getAmbariClusterServicesComponentsNamenode(instanceMap, sparkService);
        putAmbariClusterHostComponents(sparkService);
        deleteClusterHostComponents(sparkService);
        deleteAmbariClusterHost(sparkService);
        getAmbariViews(sparkService);
    }

    private void postAmbariClusters(Service sparkService) {
        sparkService.post(AMBARI_API_ROOT + "/clusters/:cluster", (req, resp) -> {
            getModel().setClusterCreated(true);
            return new EmptyAmbariResponse().handle(req, resp);
        }, gson()::toJson);
    }

    private void getAmabriVersionDefinitions(Service sparkService) {
        sparkService.get(AMBARI_API_ROOT + "/version_definitions", new AmbariVersionDefinitionResponse());
    }

    private void getAmbariClusterHosts(Map<String, CloudVmMetaDataStatus> instanceMap, Service sparkService) {
        sparkService.get(AMBARI_API_ROOT + "/clusters/:cluster/hosts", new AmbariCategorizedHostComponentStateResponse(instanceMap));
    }

    private void getAmbariClusterHosts2(Map<String, CloudVmMetaDataStatus> instanceMap, Service sparkService) {
        //sparkService.get(AMBARI_API_ROOT + "/clusters/:cluster/hosts", new AmbariClustersHostsResponse(instanceMap, "INSTALLED"));
        getAmbariClusterHosts(instanceMap, sparkService, "INSTALLED");
    }

    private void getAmbariClusterHosts(Map<String, CloudVmMetaDataStatus> instanceMap, Service sparkService, String state) {
        sparkService.get(AMBARI_API_ROOT + "/clusters/:cluster/hosts", new AmbariClustersHostsResponseW(instanceMap, state));
    }

    private void getAmbariHosts(Map<String, CloudVmMetaDataStatus> instanceMap, Service sparkService) {
        sparkService.get(AMBARI_API_ROOT + "/hosts", new AmbariHostsResponse(instanceMap), gson()::toJson);
    }

    private void getAmbariClusters(String clusterName, Map<String, CloudVmMetaDataStatus> instanceMap, Service sparkService) {
        sparkService.get(AMBARI_API_ROOT + "/clusters", (req, resp) -> {
            ITResponse itResp = getModel().isClusterCreated() ? new AmbariClusterResponse(instanceMap, clusterName) : new EmptyAmbariClusterResponse();
            return itResp.handle(req, resp);
        });
    }

    private void getAmbariClusterServicesComponentsNamenode(Map<String, CloudVmMetaDataStatus> instanceMap, Service sparkService) {
        sparkService.get(AMBARI_API_ROOT + CLUSTERS_CLUSTER_SERVICES_HDFS_COMPONENTS_NAMENODE, (request, response) -> {
            response.type("text/plain");
            ObjectNode rootNode = JsonNodeFactory.instance.objectNode();
            ObjectNode nameNode = rootNode.putObject("metrics").putObject("dfs").putObject("namenode");
            ObjectNode liveNodesRoot = JsonNodeFactory.instance.objectNode();

            for (CloudVmMetaDataStatus status : instanceMap.values()) {
                ObjectNode node = liveNodesRoot.putObject(HostNameUtil.generateHostNameByIp(status.getMetaData().getPrivateIp()));
                node.put("remaining", "10000000");
                node.put("usedSpace", Integer.toString(100000));
                node.put("adminState", "In Service");
            }

            nameNode.put("LiveNodes", liveNodesRoot.toString());
            nameNode.put("DecomNodes", "{}");
            return rootNode;
        });
    }

    private void getAmbariClusterHostStatus(Service sparkService) {
        sparkService.get(AMBARI_API_ROOT + CLUSTERS_CLUSTER_HOSTS_INTERNALHOSTNAME, (request, response) -> {
            response.type("text/plain");
            ObjectNode rootNode = JsonNodeFactory.instance.objectNode();
            rootNode.putObject("Hosts").put("public_host_name", request.params("internalhostname")).put("host_status", "HEALTHY");
            return rootNode;
        });
    }

    private void getAmbariClusterConfigurationVersions(Service sparkService) {
        sparkService.get(AMBARI_API_ROOT + CLUSTERS_CLUSTER_NAME_CONFIGURATIONS_SERVICE_CONFIG_VERSIONS,
                new AmbariServiceConfigResponse(getModel().getMockServerAddress(), getModel().getSshPort()), gson()::toJson);
    }

    private void getAmbariClusterHostComponents(Service sparkService) {
        sparkService.get(AMBARI_API_ROOT + CLUSTERS_CLUSTER_HOSTS_HOSTNAME_HOST_COMPONENTS, new AmbariComponentStatusOnHostResponse());
    }

    private void postAmbariClusterHosts(Service sparkService) {
        sparkService.post(AMBARI_API_ROOT + CLUSTERS_CLUSTER_HOSTS, new AmbariClusterRequestsResponse());
    }

    private void putAmbariClusterServices(Service sparkService) {
        sparkService.put(AMBARI_API_ROOT + CLUSTERS_CLUSTER_SERVICES, new AmbariClusterRequestsResponse());
    }

    private void getAmbariCluster(String clusterName, Map<String, CloudVmMetaDataStatus> instanceMap, Service sparkService) {
        sparkService.get(AMBARI_API_ROOT + CLUSTERS_CLUSTER, new AmbariClusterResponse(instanceMap, clusterName));
    }

    private void getAmbariViews(Service sparkService) {
        sparkService.get(AMBARI_API_ROOT + VIEWS, new AmbariViewResponse(getModel().getMockServerAddress()));
    }

    private void postAmbariVersionDefinitions(Service sparkService) {
        sparkService.post(AMBARI_API_ROOT + "/version_definitions", new EmptyAmbariResponse());
    }

    private void putAmbariHdpVersion(Service sparkService) {
        sparkService.put(AMBARI_API_ROOT + STACKS_HDP_VERSIONS_VERSION_OPERATING_SYSTEMS_OS_REPOSITORIES_HDPVERSION,
                new AmbariVersionDefinitionResponse());
    }

    private void putAmbariClusterHostComponents(Service sparkService) {
        sparkService.put(AMBARI_API_ROOT + CLUSTERS_CLUSTER_HOST_COMPONENTS, new AmbariClusterRequestsResponse());
    }

    private void deleteClusterHostComponents(Service sparkService) {
        sparkService.delete(AMBARI_API_ROOT + CLUSTERS_CLUSTER_HOSTS_HOSTNAME_HOST_COMPONENTS, new EmptyAmbariResponse());
    }

    private void deleteAmbariClusterHost(Service sparkService) {
        sparkService.delete(AMBARI_API_ROOT + CLUSTERS_CLUSTER_HOSTS_HOSTNAME, new AmbariClusterRequestsResponse());
    }

    private void postAmbariUsers(Service sparkService) {
        sparkService.post(AMBARI_API_ROOT + USERS, new EmptyAmbariResponse());
    }

    private void putAmbariUsersAdmin(Service sparkService) {
        sparkService.put(AMBARI_API_ROOT + USERS_ADMIN, new EmptyAmbariResponse());
    }

    private void postAmbariBlueprints(Service sparkService) {
        sparkService.post(AMBARI_API_ROOT + BLUEPRINTS, new EmptyAmbariResponse());
    }

    private void getAmbariBlueprint(Service sparkService) {
        sparkService.get(AMBARI_API_ROOT + BLUEPRINTS_BLUEPRINTNAME, (request, response) -> {
            response.type("text/plain");
            return responseFromJsonFile("blueprint/" + request.params("blueprintname") + ".bp");
        });
    }

    private void getAmbariComponents(Service sparkService) {
        sparkService.get(AMBARI_API_ROOT + SERVICES_AMBARI_COMPONENTS_AMBARI_SERVER, new AmbariServicesComponentsResponse(), gson()::toJson);
    }

    private void postAmbariCluster(Service sparkService) {
        sparkService.post(AMBARI_API_ROOT + CLUSTERS_CLUSTER, (request, response) -> {
            getModel().setClusterName(request.params("cluster"));
            response.type("text/plain");
            return "";
        });
    }

    private void postAmbariClusterRequest(Service sparkService) {
        sparkService.post(AMBARI_API_ROOT + CLUSTERS_CLUSTER_REQUESTS, new AmbariClusterRequestsResponse());
    }

    private void postAmbariInstances(Service sparkService) {
        sparkService.post(AMBARI_API_ROOT + VIEWS_VIEW_VERSIONS_1_0_0_INSTANCES, new EmptyAmbariResponse());
    }

    private void getAmbariClusterRequest(Service sparkService) {
        sparkService.get(AMBARI_API_ROOT + CLUSTERS_CLUSTER_REQUESTS_REQUEST, new AmbariStatusResponse());
    }

    private void getAmbariCheck(Service sparkService) {
        sparkService.get(AMBARI_API_ROOT + CHECK, new AmbariCheckResponse());
    }
}

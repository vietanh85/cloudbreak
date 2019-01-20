package com.sequenceiq.it.cloudbreak.newway.cloud;

import java.util.LinkedHashMap;
import java.util.Optional;

import com.sequenceiq.cloudbreak.api.model.rds.RDSConfigRequest;
import com.sequenceiq.cloudbreak.api.model.rds.RdsType;
import com.sequenceiq.cloudbreak.api.model.v2.CloudStorageRequest;
import com.sequenceiq.cloudbreak.api.model.v2.filesystem.CloudStorageParameters;
import com.sequenceiq.it.cloudbreak.filesystem.CloudStorageTypePathPrefix;
import com.sequenceiq.it.cloudbreak.newway.LdapConfig;
import com.sequenceiq.it.cloudbreak.newway.MissingExpectedParameterException;
import com.sequenceiq.it.cloudbreak.newway.RdsConfig;
import com.sequenceiq.it.cloudbreak.newway.TestParameter;

public abstract class ResourceHelper<T extends CloudStorageParameters> {

    private final TestParameter testParameter;

    private final String postfix;

    ResourceHelper(TestParameter testParameter) {
        this(testParameter, "");
    }

    ResourceHelper(TestParameter testParameter, String postfix) {
        this.testParameter = testParameter;
        this.postfix = postfix;
    }

    public LdapConfig aValidLdap() {
        return LdapConfig.isCreatedWithParametersAndName(testParameter, getLdapConfigName());
    }

    public String getLdapConfigName() {
        var ldapName = testParameter.get("ldapConfigName");
        return ldapName == null ? String.format("ldapconfig%s", postfix) : String.format("%s%s", ldapName, postfix);
    }

    public abstract RdsConfig aValidHiveDatabase();

    public abstract RdsConfig aValidRangerDatabase();

    public abstract CloudStorageRequest getCloudStorageRequestForDatalake();

    public abstract CloudStorageRequest getCloudStorageRequestForAttachedCluster();

    protected abstract T getCloudStorage();

    protected TestParameter getTestParameter() {
        return testParameter;
    }

    protected RDSConfigRequest createRdsRequestWithProperties(String configName, String userName, String password, String connectionUrl, RdsType rdsType) {
        var request = new RDSConfigRequest();
        request.setName(getParam(configName));
        request.setConnectionUserName(getParam(userName));
        request.setConnectionPassword(getParam(password));
        request.setConnectionURL(getParam(connectionUrl));
        request.setType(rdsType.name());
        return request;
    }

    protected StorageLocationsInternal defaultDatalakeStorageLocations(CloudStorageTypePathPrefix type, String parameterToInsert) {
        StorageLocationsInternal locations = new StorageLocationsInternal();
        locations.setOpLogs(new LinkedHashMap<>(1));
        locations.setNotebook(new LinkedHashMap<>(1));
        var warehouse = new LinkedHashMap<String, String>(1);
        warehouse.put("hiveWarehouse", String.format("%s://%s/apps/hive/warehouse", type.getPrefix(), parameterToInsert));
        locations.setWarehouse(warehouse);
        var audit = new LinkedHashMap<String, String>(1);
        audit.put("rangerAudit", String.format("%s://%s/apps/ranger/audit", type.getPrefix(), parameterToInsert));
        locations.setAudit(audit);
        return locations;
    }

    protected CloudStorageRequest getCloudStorageForAttachedCluster(CloudStorageTypePathPrefix type, String parameterToInsert,
                    CloudStorageParameters cloudStorageParameterInstance) {
        var request = new CloudStorageRequest();
        var opLogs = new LinkedHashMap<String, String>(1);
        opLogs.put("hiveWarehouse", String.format("%s://%s/attached/apps/hive/warehouse", type.getPrefix(), parameterToInsert));
        request.setOpLogs(opLogs);
        request.setNotebook(new LinkedHashMap<>(1));
        request.setWarehouse(new LinkedHashMap<>(1));
        request.setAudit(new LinkedHashMap<>(1));
        type.setParameterForRequest(request, cloudStorageParameterInstance);
        return request;
    }

    protected void setStorageLocations(CloudStorageRequest request, StorageLocationsInternal locations) {
        request.setOpLogs(locations.getOpLogs());
        request.setNotebook(locations.getNotebook());
        request.setWarehouse(locations.getWarehouse());
        request.setAudit(locations.getAudit());
    }

    private String getParam(String key) {
        return Optional.ofNullable(testParameter.get(key)).orElseThrow(() -> new MissingExpectedParameterException(key));
    }

}

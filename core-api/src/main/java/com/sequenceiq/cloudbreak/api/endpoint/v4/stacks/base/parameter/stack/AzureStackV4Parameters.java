package com.sequenceiq.cloudbreak.api.endpoint.v4.stacks.base.parameter.stack;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.sequenceiq.cloudbreak.api.endpoint.v4.common.mappable.CloudPlatform;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AzureStackV4Parameters extends StackV4ParameterBase {

    @ApiModelProperty
    private String resourceGroupName;

    @ApiModelProperty
    private boolean encryptStorage;

    @ApiModelProperty
    private boolean yarnQueue;

    public String getResourceGroupName() {
        return resourceGroupName;
    }

    public void setResourceGroupName(String resourceGroupName) {
        this.resourceGroupName = resourceGroupName;
    }

    public boolean isEncryptStorage() {
        return encryptStorage;
    }

    public void setEncryptStorage(boolean encryptStorage) {
        this.encryptStorage = encryptStorage;
    }

    public boolean isYarnQueue() {
        return yarnQueue;
    }

    public void setYarnQueue(boolean yarnQueue) {
        this.yarnQueue = yarnQueue;
    }

    @Override
    public Map<String, Object> asMap() {
        Map<String, Object> map = super.asMap();
        putIfValueNotNull(map, "resourceGroupName", resourceGroupName);
        putIfValueNotNull(map, "encryptStorage", encryptStorage);
        putIfValueNotNull(map, "yarnQueue", yarnQueue);
        return map;
    }

    @Override
    @JsonIgnore
    @ApiModelProperty(hidden = true)
    public CloudPlatform getCloudPlatform() {
        return CloudPlatform.AZURE;
    }

    @Override
    public void parse(Map<String, Object> parameters) {
        resourceGroupName = getParameterOrNull(parameters, "resourceGroupName");
        encryptStorage = getBoolean(parameters, "encryptStorage");
        yarnQueue = getBoolean(parameters, "yarnQueue");
    }
}

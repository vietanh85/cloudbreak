package com.sequenceiq.cloudbreak.api.model.v2;


import com.sequenceiq.cloudbreak.api.model.JsonEntity;

import io.swagger.annotations.ApiModelProperty;

public class StorageLocationBase implements JsonEntity {

    @ApiModelProperty
    private String configFile;

    @ApiModelProperty
    private String property;

    @ApiModelProperty
    private String value;

    public String getConfigFile() {
        return configFile;
    }

    public void setConfigFile(String configFile) {
        this.configFile = configFile;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

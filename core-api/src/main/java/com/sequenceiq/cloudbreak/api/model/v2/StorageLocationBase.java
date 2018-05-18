package com.sequenceiq.cloudbreak.api.model.v2;


import com.sequenceiq.cloudbreak.api.model.JsonEntity;

import io.swagger.annotations.ApiModelProperty;

public class StorageLocationBase implements JsonEntity {

    @ApiModelProperty
    private String propertyFile;

    @ApiModelProperty
    private String propertyName;

    @ApiModelProperty
    private String value;

    public String getPropertyFile() {
        return propertyFile;
    }

    public void setPropertyFile(String propertyFile) {
        this.propertyFile = propertyFile;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

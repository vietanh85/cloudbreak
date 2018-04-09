package com.sequenceiq.cloudbreak.api.model;

import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.sequenceiq.cloudbreak.doc.ModelDescriptions;
import com.sequenceiq.cloudbreak.doc.ModelDescriptions.BlueprintModelDescription;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BlueprintRequest extends BlueprintBase {

    @Size(max = 100, min = 5, message = "The length of the blueprint's name has to be in range of 1 to 100")
    @Pattern(regexp = "(^[a-z][-a-z0-9]*[a-z0-9]$)",
            message = "The name can only contain lowercase alphanumeric characters and hyphens and has start with an alphanumeric character")
    @NotNull
    @ApiModelProperty(value = ModelDescriptions.NAME, required = true)
    private String name;

    @Size(max = 100, message = "The length of the blueprint's display name has to be in range of 0 to 100")
    @ApiModelProperty(value = ModelDescriptions.NAME, required = true)
    private String displayName;

    @ApiModelProperty(BlueprintModelDescription.URL)
    private String url;

    @ApiModelProperty(BlueprintModelDescription.BLUEPRINT_PROPERTIES)
    private List<Map<String, Map<String, String>>> properties;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<Map<String, Map<String, String>>> getProperties() {
        return properties;
    }

    public void setProperties(List<Map<String, Map<String, String>>> properties) {
        this.properties = properties;
    }
}

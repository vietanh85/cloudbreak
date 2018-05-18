package com.sequenceiq.cloudbreak.api.model;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.sequenceiq.cloudbreak.doc.ModelDescriptions;

import io.swagger.annotations.ApiModelProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StructuredParameterQueriesResponse implements JsonEntity {

    @ApiModelProperty(value = ModelDescriptions.StackModelDescription.ENTRIES, required = true)
    private Set<StructuredParameterQueryResponse> entries;

    public Set<StructuredParameterQueryResponse> getEntries() {
        return entries;
    }

    public void setEntries(Set<StructuredParameterQueryResponse> entries) {
        this.entries = entries;
    }
}

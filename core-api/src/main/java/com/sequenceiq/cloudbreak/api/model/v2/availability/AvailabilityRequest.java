package com.sequenceiq.cloudbreak.api.model.v2.availability;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModel;

@ApiModel
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AvailabilityRequest {

    private AwsParameters awsParameters;

    private AzureParameters azureParameters;

    private GcpParameters gcpParameters;

    private OpenStackParameters openStackParameters;

    public AwsParameters getAwsParameters() {
        return awsParameters;
    }

    public void setAwsParameters(AwsParameters awsParameters) {
        this.awsParameters = awsParameters;
    }

    public AzureParameters getAzureParameters() {
        return azureParameters;
    }

    public void setAzureParameters(AzureParameters azureParameters) {
        this.azureParameters = azureParameters;
    }

    public GcpParameters getGcpParameters() {
        return gcpParameters;
    }

    public void setGcpParameters(GcpParameters gcpParameters) {
        this.gcpParameters = gcpParameters;
    }

    public OpenStackParameters getOpenStackParameters() {
        return openStackParameters;
    }

    public void setOpenStackParameters(OpenStackParameters openStackParameters) {
        this.openStackParameters = openStackParameters;
    }
}

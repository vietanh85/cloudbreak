package com.sequenceiq.cloudbreak.api.model.v2.availability;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.sequenceiq.cloudbreak.doc.ModelDescriptions;
import com.sequenceiq.cloudbreak.validation.ValidSubnet;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AvailabilityRequest {

    @ApiModelProperty(ModelDescriptions.StackModelDescription.AVAILABILITY_ZONE)
    private String availabilityZone;

    @ApiModelProperty(ModelDescriptions.NetworkModelDescription.SUBNET_CIDR)
    @ValidSubnet
    private String subnetCIDR;

    @ApiModelProperty
    private AwsParameters awsParameters;

    @ApiModelProperty
    private AzureParameters azureParameters;

    @ApiModelProperty
    private GcpParameters gcpParameters;

    @ApiModelProperty
    private OpenStackParameters openStackParameters;

    @ApiModelProperty
    private YarnParameters yarnParameters;

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

    public String getAvailabilityZone() {
        return availabilityZone;
    }

    public void setAvailabilityZone(String availabilityZone) {
        this.availabilityZone = availabilityZone;
    }

    public String getSubnetCIDR() {
        return subnetCIDR;
    }

    public void setSubnetCIDR(String subnetCIDR) {
        this.subnetCIDR = subnetCIDR;
    }

    public YarnParameters getYarnParameters() {
        return yarnParameters;
    }

    public void setYarnParameters(YarnParameters yarnParameters) {
        this.yarnParameters = yarnParameters;
    }
}

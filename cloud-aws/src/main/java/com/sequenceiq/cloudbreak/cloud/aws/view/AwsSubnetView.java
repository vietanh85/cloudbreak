package com.sequenceiq.cloudbreak.cloud.aws.view;

public class AwsSubnetView {

    private String subnetCidr;

    private String id;

    public String getSubnetCidr() {
        return subnetCidr;
    }

    public void setSubnetCidr(String subnetCidr) {
        this.subnetCidr = subnetCidr;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}

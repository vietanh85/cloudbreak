package com.sequenceiq.cloudbreak.cloud.model;

public class CloudAvailability {

    private Subnet subnet;

    private String availabiltyZone;

    public CloudAvailability(Subnet subnet, String availabiltyZone) {
        this.subnet = subnet;
        this.availabiltyZone = availabiltyZone;
    }

    public Subnet getSubnet() {
        return subnet;
    }

    public String getAvailabiltyZone() {
        return availabiltyZone;
    }
}

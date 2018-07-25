package com.sequenceiq.cloudbreak.cloud.model;

import java.util.Map;
import java.util.Objects;

import com.sequenceiq.cloudbreak.cloud.model.generic.DynamicModel;

public class Subnet extends DynamicModel {

    private final String cidr;

    public Subnet(String cidr, Map<String, Object> parameters) {
        super(parameters);
        this.cidr = cidr;
    }

    public String getCidr() {
        return cidr;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Subnet)) {
            return false;
        }
        Subnet subnet = (Subnet) o;
        return Objects.equals(getCidr(), subnet.getCidr());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCidr());
    }

}

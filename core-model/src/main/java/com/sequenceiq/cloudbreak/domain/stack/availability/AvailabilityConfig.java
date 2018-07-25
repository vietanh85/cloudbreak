package com.sequenceiq.cloudbreak.domain.stack.availability;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;

import com.sequenceiq.cloudbreak.domain.ProvisionEntity;
import com.sequenceiq.cloudbreak.domain.json.Json;
import com.sequenceiq.cloudbreak.domain.json.JsonToString;
import com.sequenceiq.cloudbreak.domain.stack.instance.InstanceGroup;

@Entity
public class AvailabilityConfig implements ProvisionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "availabilityconfig_generator")
    @SequenceGenerator(name = "availabilityconfig_generator", sequenceName = "availabilityconfig_id_seq", allocationSize = 1)
    private Long id;

    private String availabilityZone;

    private String subnetCIDR;

    @Convert(converter = JsonToString.class)
    @Column(columnDefinition = "TEXT")
    private Json configurations;

    @ManyToOne
    private InstanceGroup instanceGroup;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Json getConfigurations() {
        return configurations;
    }

    public void setConfigurations(Json configurations) {
        this.configurations = configurations;
    }

    public InstanceGroup getInstanceGroup() {
        return instanceGroup;
    }

    public void setInstanceGroup(InstanceGroup instanceGroup) {
        this.instanceGroup = instanceGroup;
    }
}

package com.sequenceiq.cloudbreak.domain.stack.availability;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

import com.sequenceiq.cloudbreak.domain.ProvisionEntity;
import com.sequenceiq.cloudbreak.domain.json.Json;
import com.sequenceiq.cloudbreak.domain.json.JsonToString;

@Entity
public class AvailabilityConfig implements ProvisionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "availabilityconfig_generator")
    @SequenceGenerator(name = "availabilityconfig_generator", sequenceName = "availabilityconfig_id_seq", allocationSize = 1)
    private Long id;

    @Convert(converter = JsonToString.class)
    @Column(columnDefinition = "TEXT")
    private Json configurations;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Json getConfigurations() {
        return configurations;
    }

    public void setConfigurations(Json configurations) {
        this.configurations = configurations;
    }
}

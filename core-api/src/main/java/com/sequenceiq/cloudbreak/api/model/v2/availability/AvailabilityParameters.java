package com.sequenceiq.cloudbreak.api.model.v2.availability;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sequenceiq.cloudbreak.api.model.JsonEntity;

@JsonIgnoreProperties(ignoreUnknown = true, value = "type")
public interface AvailabilityParameters extends JsonEntity {

    String getType();

}

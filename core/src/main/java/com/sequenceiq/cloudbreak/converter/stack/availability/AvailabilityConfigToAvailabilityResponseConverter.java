package com.sequenceiq.cloudbreak.converter.stack.availability;

import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.api.model.v2.availability.AvailabilityResponse;
import com.sequenceiq.cloudbreak.converter.AbstractConversionServiceAwareConverter;
import com.sequenceiq.cloudbreak.domain.stack.availability.AvailabilityConfig;

@Component
public class AvailabilityConfigToAvailabilityResponseConverter extends AbstractConversionServiceAwareConverter<AvailabilityConfig, AvailabilityResponse> {

    @Override
    public AvailabilityResponse convert(AvailabilityConfig entity) {
        AvailabilityResponse availabilityResponse = new AvailabilityResponse();
        availabilityResponse.setSubnetCIDR(entity.getSubnetCIDR());
        availabilityResponse.setAvailabilityZone(entity.getAvailabilityZone());
        if (entity.getConfigurations() != null && entity.getConfigurations().getValue() != null) {
            availabilityResponse.setParameters(entity.getConfigurations().getMap());
        }
        return availabilityResponse;
    }

}

package com.sequenceiq.cloudbreak.converter.stack.availability;

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.api.model.v2.availability.AvailabilityResponse;
import com.sequenceiq.cloudbreak.api.model.v2.availability.AwsParameters;
import com.sequenceiq.cloudbreak.api.model.v2.availability.AzureParameters;
import com.sequenceiq.cloudbreak.api.model.v2.availability.GcpParameters;
import com.sequenceiq.cloudbreak.api.model.v2.availability.OpenStackParameters;
import com.sequenceiq.cloudbreak.api.model.v2.availability.YarnParameters;
import com.sequenceiq.cloudbreak.common.type.CloudConstants;
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
            String cloudPlatform = entity.getInstanceGroup().getTemplate().cloudPlatform();
            try {
                switch (cloudPlatform) {
                    case CloudConstants.AWS:
                        availabilityResponse.setAwsParameters(entity.getConfigurations().get(AwsParameters.class));
                    case CloudConstants.AZURE:
                        availabilityResponse.setAzureParameters(entity.getConfigurations().get(AzureParameters.class));
                    case CloudConstants.GCP:
                        availabilityResponse.setGcpParameters(entity.getConfigurations().get(GcpParameters.class));
                    case CloudConstants.OPENSTACK:
                        availabilityResponse.setOpenStackParameters(entity.getConfigurations().get(OpenStackParameters.class));
                    case CloudConstants.YARN:
                        availabilityResponse.setYarnParameters(entity.getConfigurations().get(YarnParameters.class));
                    default:
                        break;
                }
            } catch (IOException e) {
                // TODO log
                e.printStackTrace();
            }
        }
        return availabilityResponse;
    }

}

package com.sequenceiq.cloudbreak.converter.spi;

import com.sequenceiq.cloudbreak.api.model.v2.filesystem.WasbCloudStorageParameters;
import com.sequenceiq.cloudbreak.cloud.model.filesystem.CloudWasbView;
import com.sequenceiq.cloudbreak.converter.AbstractConversionServiceAwareConverter;

public class WasbCloudStorageParametersToCloudWasbView
        extends AbstractConversionServiceAwareConverter<WasbCloudStorageParameters, CloudWasbView> {
    @Override
    public CloudWasbView convert(WasbCloudStorageParameters source) {
        CloudWasbView cloudWasbView = new CloudWasbView();
        cloudWasbView.setAccountKey(source.getAccountKey());
        cloudWasbView.setAccountName(source.getAccountName());
        return cloudWasbView;
    }
}

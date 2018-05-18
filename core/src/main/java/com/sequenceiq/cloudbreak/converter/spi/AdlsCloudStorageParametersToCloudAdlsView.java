package com.sequenceiq.cloudbreak.converter.spi;

import com.sequenceiq.cloudbreak.api.model.v2.filesystem.AdlsCloudStorageParameters;
import com.sequenceiq.cloudbreak.cloud.model.filesystem.CloudAdlsView;
import com.sequenceiq.cloudbreak.converter.AbstractConversionServiceAwareConverter;

public class AdlsCloudStorageParametersToCloudAdlsView
        extends AbstractConversionServiceAwareConverter<AdlsCloudStorageParameters, CloudAdlsView> {
    @Override
    public CloudAdlsView convert(AdlsCloudStorageParameters source) {
        CloudAdlsView cloudAdlsView = new CloudAdlsView();
        cloudAdlsView.setAccountName(source.getAccountName());
        cloudAdlsView.setClientId(source.getClientId());
        cloudAdlsView.setCredential(source.getCredential());
        cloudAdlsView.setTenantId(source.getTenantId());
        return cloudAdlsView;
    }
}

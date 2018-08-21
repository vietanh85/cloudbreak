package com.sequenceiq.cloudbreak.cloud.k8s;

import org.springframework.stereotype.Service;

import com.sequenceiq.cloudbreak.cloud.Setup;
import com.sequenceiq.cloudbreak.cloud.context.AuthenticatedContext;
import com.sequenceiq.cloudbreak.cloud.model.CloudCredential;
import com.sequenceiq.cloudbreak.cloud.model.CloudStack;
import com.sequenceiq.cloudbreak.cloud.model.Image;
import com.sequenceiq.cloudbreak.cloud.model.SpiFileSystem;
import com.sequenceiq.cloudbreak.cloud.notification.PersistenceNotifier;
import com.sequenceiq.cloudbreak.common.type.ImageStatus;
import com.sequenceiq.cloudbreak.common.type.ImageStatusResult;

@Service
public class K8sProvisionSetup implements Setup {
    @Override
    public void prepareImage(AuthenticatedContext authenticatedContext, CloudStack stack, Image image) {
        // There is no image create step in K8s Cloud
    }

    @Override
    public ImageStatusResult checkImageStatus(AuthenticatedContext authenticatedContext, CloudStack stack, Image image) {
        return new ImageStatusResult(ImageStatus.CREATE_FINISHED, ImageStatusResult.COMPLETED);
    }

    @Override
    public void prerequisites(AuthenticatedContext authenticatedContext, CloudStack stack, PersistenceNotifier persistenceNotifier) {

    }

    @Override
    public void validateFileSystem(CloudCredential credential, SpiFileSystem spiFileSystem) {

    }

    @Override
    public void scalingPrerequisites(AuthenticatedContext authenticatedContext, CloudStack stack, boolean upscale) {

    }
}

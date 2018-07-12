package com.sequenceiq.cloudbreak.core.flow2.stack.image.update;

import static com.sequenceiq.cloudbreak.cloud.model.Platform.platform;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sequenceiq.cloudbreak.cloud.VersionComparator;
import com.sequenceiq.cloudbreak.cloud.event.model.EventStatus;
import com.sequenceiq.cloudbreak.cloud.model.CloudbreakDetails;
import com.sequenceiq.cloudbreak.cloud.model.Image;
import com.sequenceiq.cloudbreak.common.type.ComponentType;
import com.sequenceiq.cloudbreak.controller.exception.CloudbreakApiException;
import com.sequenceiq.cloudbreak.core.CloudbreakImageCatalogException;
import com.sequenceiq.cloudbreak.core.CloudbreakImageNotFoundException;
import com.sequenceiq.cloudbreak.domain.json.Json;
import com.sequenceiq.cloudbreak.domain.stack.Component;
import com.sequenceiq.cloudbreak.domain.stack.Stack;
import com.sequenceiq.cloudbreak.service.CloudbreakServiceException;
import com.sequenceiq.cloudbreak.service.ComponentConfigProvider;
import com.sequenceiq.cloudbreak.service.image.ImageCatalogService;
import com.sequenceiq.cloudbreak.service.image.ImageService;
import com.sequenceiq.cloudbreak.service.image.StatedImage;
import com.sequenceiq.cloudbreak.service.stack.StackService;
import com.sequenceiq.cloudbreak.service.stack.connector.OperationException;

@Service
public class StackImageUpdateService {

    public static final String MIN_VERSION = "2.8.0";

    private static final Logger LOGGER = LoggerFactory.getLogger(StackImageUpdateService.class);

    @Inject
    private ComponentConfigProvider componentConfigProvider;

    @Inject
    private ImageCatalogService imageCatalogService;

    @Inject
    private ImageService imageService;

    @Inject
    private StackService stackService;

    public void storeNewImageComponent(Stack stack, StatedImage image) {
        Component imageComponent;
        try {
            Image currentImage = getCurrentImage(stack);
            String platformString = platform(stack.cloudPlatform()).value().toLowerCase();

            String newImageName = imageService.determineImageName(platformString, stack.getRegion(), image.getImage());
            Image newImage = new Image(newImageName, currentImage.getUserdata(), image.getImage().getOs(), image.getImage().getOsType(),
                    image.getImageCatalogUrl(), image.getImageCatalogName(), image.getImage().getUuid());
            imageComponent = new Component(ComponentType.IMAGE, ComponentType.IMAGE.name(), new Json(newImage), stack);
        } catch (JsonProcessingException e) {
            LOGGER.error("Failed to create json", e);
            throw new CloudbreakServiceException("Failed to create json", e);
        } catch (CloudbreakImageNotFoundException e) {
            LOGGER.error("Could not find image", e);
            throw new CloudbreakServiceException("Could not find image", e);
        }
        componentConfigProvider.replaceImageComponentWithNew(imageComponent);
    }

    public StatedImage getNewImageIfVersionsMatch(Stack stack, String newImageId, String imageCatalogName, String imageCatalogUrl) {
        try {
            Image currentImage = getCurrentImage(stack);

            StatedImage newImage = getNewImage(newImageId, imageCatalogName, imageCatalogUrl, currentImage);

            if (isOsVersionsMatch(currentImage, newImage)) {
                String message = String.format("New image OS [%s] and OS type [%s] is different from current OS [%s] and OS type [%s]",
                        newImage.getImage().getOs(), newImage.getImage().getOsType(), currentImage.getOs(), currentImage.getOsType());
                LOGGER.warn(message);
                throw new OperationException(message);
            }
            return newImage;

        } catch (CloudbreakImageNotFoundException e) {
            LOGGER.error("Cloudbreak Image not found", e);
            throw new CloudbreakApiException(e.getMessage(), e);
        } catch (CloudbreakImageCatalogException e) {
            LOGGER.error("Cloudbreak Image Catalog error", e);
            throw new CloudbreakApiException(e.getMessage(), e);
        }
    }

    private Image getCurrentImage(Stack stack) throws CloudbreakImageNotFoundException {
        return componentConfigProvider.getImage(stack.getId());
    }

    private boolean isOsVersionsMatch(Image currentImage, StatedImage newImage) {
        return !newImage.getImage().getOs().equals(currentImage.getOs()) || !newImage.getImage().getOsType().equals(currentImage.getOsType());
    }

    private StatedImage getNewImage(String newImageId, String imageCatalogName, String imageCatalogUrl, Image currentImage)
            throws CloudbreakImageNotFoundException, CloudbreakImageCatalogException {
        StatedImage newImage;
        if (StringUtils.isNotBlank(imageCatalogName) && StringUtils.isNotBlank(imageCatalogUrl)) {
            newImage = imageCatalogService.getImage(imageCatalogUrl, imageCatalogName, newImageId);
        } else {
            newImage = imageCatalogService.getImage(currentImage.getImageCatalogUrl(), currentImage.getImageCatalogName(), newImageId);
        }
        return newImage;
    }

    public boolean isCbVersionOk(Stack stack) {
        CloudbreakDetails cloudbreakDetails = componentConfigProvider.getCloudbreakDetails(stack.getId());
        VersionComparator versionComparator = new VersionComparator();
        String version = StringUtils.substringBefore(cloudbreakDetails.getVersion(), "-");
        int compare = versionComparator.compare(() -> version, () -> MIN_VERSION);
        return compare >= 0;
    }

    public CheckResult checkPackageVersions(Stack stack, StatedImage newImage) {
        return CheckResult.ok();
    }

    public boolean isValidImage(Stack stack, String newImageId, String imageCatalogName, String imageCatalogUrl) {
        if (isCbVersionOk(stack)) {
            try {
                StatedImage newImage = getNewImage(newImageId, imageCatalogName, imageCatalogUrl, getCurrentImage(stack));
                return checkPackageVersions(stack, newImage).getStatus() == EventStatus.OK;
            } catch (CloudbreakImageNotFoundException e) {
                LOGGER.warn("Cloudbreak Image not found", e);
                return false;
            } catch (CloudbreakImageCatalogException e) {
                LOGGER.warn("Cloudbreak Image Catalog error", e);
                return false;
            }
        }
        return false;
    }

}

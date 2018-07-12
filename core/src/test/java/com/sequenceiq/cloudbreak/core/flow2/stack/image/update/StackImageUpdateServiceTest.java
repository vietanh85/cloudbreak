package com.sequenceiq.cloudbreak.core.flow2.stack.image.update;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sequenceiq.cloudbreak.cloud.model.CloudbreakDetails;
import com.sequenceiq.cloudbreak.cloud.model.catalog.Image;
import com.sequenceiq.cloudbreak.cloud.model.catalog.StackDetails;
import com.sequenceiq.cloudbreak.cloud.model.catalog.StackRepoDetails;
import com.sequenceiq.cloudbreak.common.type.ComponentType;
import com.sequenceiq.cloudbreak.core.CloudbreakImageCatalogException;
import com.sequenceiq.cloudbreak.core.CloudbreakImageNotFoundException;
import com.sequenceiq.cloudbreak.domain.stack.Component;
import com.sequenceiq.cloudbreak.domain.stack.Stack;
import com.sequenceiq.cloudbreak.service.ComponentConfigProvider;
import com.sequenceiq.cloudbreak.service.image.ImageCatalogService;
import com.sequenceiq.cloudbreak.service.image.ImageService;
import com.sequenceiq.cloudbreak.service.image.StatedImage;
import com.sequenceiq.cloudbreak.service.stack.StackService;
import com.sequenceiq.cloudbreak.service.stack.connector.OperationException;

public class StackImageUpdateServiceTest {

    @Mock
    private ComponentConfigProvider componentConfigProvider;

    @Mock
    private ImageCatalogService imageCatalogService;

    @Mock
    private ImageService imageService;

    @Mock
    private StackService stackService;

    @InjectMocks
    private StackImageUpdateService underTest;

    private Stack stack;

    private StatedImage statedImage;

    private Image image;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        stack = new Stack();
        stack.setId(1L);
        stack.setName("stackname");
        stack.setRegion("region");
        stack.setCloudPlatform("AWS");

        StackRepoDetails repo = new StackRepoDetails(Collections.emptyMap(), Collections.emptyMap());
        StackDetails stackDetails = new StackDetails("2", repo, Collections.emptyList());
        image = new Image("asdf", "asdf", "centos7", "uuid", "2.8.0", Collections.emptyMap(), Collections.emptyMap(),
                stackDetails, "centos");
        statedImage = StatedImage.statedImage(image, "url", "name");
    }

    @Test
    public void teststoreNewImageComponent() throws CloudbreakImageNotFoundException, IOException {

        com.sequenceiq.cloudbreak.cloud.model.Image imageInComponent =
                new com.sequenceiq.cloudbreak.cloud.model.Image("imageOldName", Collections.emptyMap(), image.getOs(), image.getOsType(),
                        statedImage.getImageCatalogUrl(), statedImage.getImageCatalogName(), "uuid2");
        when(componentConfigProvider.getImage(anyLong())).thenReturn(imageInComponent);

        String imagename = "imagename";
        when(imageService.determineImageName(anyString(), anyString(), eq(image))).thenReturn(imagename);

        underTest.storeNewImageComponent(stack, statedImage);

        ArgumentCaptor<Component> captor = ArgumentCaptor.forClass(Component.class);
        verify(componentConfigProvider).replaceImageComponentWithNew(captor.capture());
        assertEquals(ComponentType.IMAGE, captor.getValue().getComponentType());
        assertEquals(ComponentType.IMAGE.name(), captor.getValue().getName());
        assertEquals(imagename, captor.getValue().getAttributes().get(com.sequenceiq.cloudbreak.cloud.model.Image.class).getImageName());
        assertEquals(image.getUuid(), captor.getValue().getAttributes().get(com.sequenceiq.cloudbreak.cloud.model.Image.class).getImageId());
    }

    @Test
    public void testIsCbVersionOk() {
        CloudbreakDetails cloudbreakDetails = new CloudbreakDetails();
        cloudbreakDetails.setVersion(StackImageUpdateService.MIN_VERSION);
        when(componentConfigProvider.getCloudbreakDetails(stack.getId())).thenReturn(cloudbreakDetails);
        assertTrue(underTest.isCbVersionOk(stack));
        cloudbreakDetails.setVersion("2.6.0");
        assertFalse(underTest.isCbVersionOk(stack));
    }

    @Test
    public void testGetNewImageIfVersionsMatch() throws CloudbreakImageNotFoundException, CloudbreakImageCatalogException {
        com.sequenceiq.cloudbreak.cloud.model.Image imageInComponent =
                new com.sequenceiq.cloudbreak.cloud.model.Image("imageOldName", Collections.emptyMap(), "centos7", "centos",
                        statedImage.getImageCatalogUrl(), statedImage.getImageCatalogName(), "uuid2");
        when(componentConfigProvider.getImage(anyLong())).thenReturn(imageInComponent);
        when(imageCatalogService.getImage(anyString(), anyString(), anyString())).thenReturn(statedImage);
        StatedImage newImageIfVersionsMatch = underTest.getNewImageIfVersionsMatch(stack, "newimageid", "imagecatalogname", "imagecatalogurl");
        assertNotNull(newImageIfVersionsMatch);
    }

    @Test(expected = OperationException.class)
    public void testGetNewImageIfVersionsMatchFail() throws CloudbreakImageNotFoundException, CloudbreakImageCatalogException {
        com.sequenceiq.cloudbreak.cloud.model.Image imageInComponent =
                new com.sequenceiq.cloudbreak.cloud.model.Image("imageOldName", Collections.emptyMap(), "centos6", "centos",
                        statedImage.getImageCatalogUrl(), statedImage.getImageCatalogName(), "uuid2");
        when(componentConfigProvider.getImage(anyLong())).thenReturn(imageInComponent);
        when(imageCatalogService.getImage(anyString(), anyString(), anyString())).thenReturn(statedImage);
        underTest.getNewImageIfVersionsMatch(stack, "newimageid", "imagecatalogname", "imagecatalogurl");
    }
}
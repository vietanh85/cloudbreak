package com.sequenceiq.cloudbreak.cloud.aws.component;

import static com.sequenceiq.cloudbreak.cloud.aws.TestConstants.INSTANCE_STATE_RUNNING;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.junit.Test;

import com.amazonaws.services.autoscaling.model.AutoScalingGroup;
import com.amazonaws.services.autoscaling.model.DescribeAutoScalingGroupsResult;
import com.amazonaws.services.autoscaling.model.DescribeScalingActivitiesResult;
import com.amazonaws.services.autoscaling.model.Instance;
import com.amazonaws.services.autoscaling.model.LifecycleState;
import com.amazonaws.services.cloudformation.model.DescribeStackResourceResult;
import com.amazonaws.services.cloudformation.model.StackResourceDetail;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusResult;
import com.amazonaws.services.ec2.model.InstanceState;
import com.sequenceiq.cloudbreak.cloud.aws.client.AmazonAutoScalingRetryClient;
import com.sequenceiq.cloudbreak.cloud.aws.client.AmazonCloudFormationRetryClient;
import com.sequenceiq.cloudbreak.cloud.aws.connector.resource.AwsResourceConnector;
import com.sequenceiq.cloudbreak.cloud.aws.encryption.EncryptedImageCopyService;
import com.sequenceiq.cloudbreak.cloud.model.CloudResource;
import com.sequenceiq.cloudbreak.cloud.model.CloudStack;
import com.sequenceiq.cloudbreak.cloud.model.InstanceStatus;
import com.sequenceiq.cloudbreak.cloud.notification.PersistenceNotifier;
import com.sequenceiq.cloudbreak.common.type.ResourceType;
import com.sequenceiq.cloudbreak.util.FreeMarkerTemplateUtils;

public class AwsUpscaleTest extends AwsComponentTest {

    private static final String STACK_PHYSICAL_RESOURCE_ID = "stackPhysicalResourceId";

    private static final String AUTOSCALING_GROUP_NAME = "autoscalingGroupName";

    private static final String INSTANCE_ID_1 = "instanceId-1";

    private static final String INSTANCE_ID_2 = "instanceId-2";

    @Inject
    private AwsResourceConnector awsResourceConnector;

    @Inject
    private PersistenceNotifier persistenceNotifier;

    @Inject
    private AmazonCloudFormationRetryClient amazonCloudFormationRetryClient;

    @Inject
    private EncryptedImageCopyService encryptedImageCopyService;

    @Inject
    private freemarker.template.Configuration configuration;

    @Inject
    private FreeMarkerTemplateUtils freeMarkerTemplateUtils;

    @Inject
    private AmazonEC2Client amazonEC2Client;

    @Inject
    private AmazonAutoScalingRetryClient amazonAutoScalingRetryClient;

    @Test
    public void testUpscaleWhenNoReattach() throws IOException {
        setupDescribeStackResourceResponse();
        setupAutoscalingResponses();
        setupDescribeInstanceStatusResponse();

        CloudStack stackToUpscale = new CloudStackBuilder()
                .withGroup(
                        new GroupBuilder()
                                .withInstance(
                                        new CloudInstanceBuilder()
                                                .withInstanceStatus(InstanceStatus.CREATED)
                                                .build()
                                )
                                .withInstance(
                                        new CloudInstanceBuilder()
                                                .withInstanceStatus(InstanceStatus.CREATE_REQUESTED)
                                                .build()
                                )
                                .build()
                )
                .build();
        List<CloudResource> existingResources = setupExistingResources();

//        awsResourceConnector.upscale(getAuthenticatedContext(), stackToUpscale, existingResources);
//
//        InOrder inOrder = inOrder(amazonAutoScalingRetryClient, amazonCloudFormationRetryClient);
//        inOrder.verify(amazonAutoScalingRetryClient).resumeProcesses(any());
//        inOrder.verify(amazonAutoScalingRetryClient).updateAutoScalingGroup(any());
//        inOrder.verify(amazonAutoScalingRetryClient).suspendProcesses(any());
//        inOrder.verify(amazonCloudFormationRetryClient).describeStackResource(any());
//        inOrder.verify(amazonAutoScalingRetryClient).describeAutoScalingGroups(any());
//        inOrder.verify(amazonAutoScalingRetryClient).describeScalingActivities(any());
//        inOrder.verify(amazonAutoScalingRetryClient).suspendProcesses(any());
//        inOrder.verify(amazonAutoScalingRetryClient).describeAutoScalingGroups(any());
    }

    // POSSIBLE DUPLICATE in AwsLaunchTest
    private void setupAutoscalingResponses() {
        when(amazonAutoScalingRetryClient.describeScalingActivities(any())).thenReturn(new DescribeScalingActivitiesResult());

        DescribeAutoScalingGroupsResult describeAutoScalingGroupsResult = new DescribeAutoScalingGroupsResult()
                .withAutoScalingGroups(
                        new AutoScalingGroup()
                                .withInstances(new Instance().withLifecycleState(LifecycleState.InService).withInstanceId(INSTANCE_ID_1))
                                .withInstances(new Instance().withLifecycleState(LifecycleState.InService).withInstanceId(INSTANCE_ID_2))
                                .withAutoScalingGroupName(AUTOSCALING_GROUP_NAME)
                );
        when(amazonAutoScalingRetryClient.describeAutoScalingGroups(any())).thenReturn(describeAutoScalingGroupsResult);
    }

    // POSSIBLE DUPLICATE in AwsLaunchTest
    private void setupDescribeInstanceStatusResponse() {
        when(amazonEC2Client.describeInstanceStatus(any())).thenReturn(
                new DescribeInstanceStatusResult().withInstanceStatuses(
                        new com.amazonaws.services.ec2.model.InstanceStatus().withInstanceState(new InstanceState().withCode(INSTANCE_STATE_RUNNING)),
                        new com.amazonaws.services.ec2.model.InstanceStatus().withInstanceState(new InstanceState().withCode(INSTANCE_STATE_RUNNING))
                )
        );
    }

    private void setupDescribeStackResourceResponse() {
        when(amazonCloudFormationRetryClient.describeStackResource(any())).thenReturn(
                new DescribeStackResourceResult().withStackResourceDetail(
                        new StackResourceDetail().withPhysicalResourceId(STACK_PHYSICAL_RESOURCE_ID)
                )
        );
    }

    private List<CloudResource> setupExistingResources() {
        return List.of(
                CloudResource.builder()
                        .name("cf")
                        .type(ResourceType.CLOUDFORMATION_STACK)
                        .params(Collections.singletonMap(CloudResource.IMAGE, "dummy"))
                        .build()
        );
    }
}

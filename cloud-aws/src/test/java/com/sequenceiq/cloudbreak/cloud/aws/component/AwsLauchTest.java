package com.sequenceiq.cloudbreak.cloud.aws.component;

import static com.sequenceiq.cloudbreak.cloud.aws.TestConstants.INSTANCE_STATE_RUNNING;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.autoscaling.model.AutoScalingGroup;
import com.amazonaws.services.autoscaling.model.DescribeAutoScalingGroupsResult;
import com.amazonaws.services.autoscaling.model.DescribeScalingActivitiesResult;
import com.amazonaws.services.autoscaling.model.Instance;
import com.amazonaws.services.autoscaling.model.LifecycleState;
import com.amazonaws.services.cloudformation.model.DescribeStackResourceResult;
import com.amazonaws.services.cloudformation.model.DescribeStacksResult;
import com.amazonaws.services.cloudformation.model.Output;
import com.amazonaws.services.cloudformation.model.Stack;
import com.amazonaws.services.cloudformation.model.StackResourceDetail;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.CreateVolumeResult;
import com.amazonaws.services.ec2.model.DescribeImagesResult;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusResult;
import com.amazonaws.services.ec2.model.DescribeVolumesRequest;
import com.amazonaws.services.ec2.model.DescribeVolumesResult;
import com.amazonaws.services.ec2.model.InstanceState;
import com.amazonaws.services.ec2.model.VolumeState;
import com.google.common.collect.ImmutableList;
import com.sequenceiq.cloudbreak.api.model.AdjustmentType;
import com.sequenceiq.cloudbreak.cloud.aws.client.AmazonAutoScalingRetryClient;
import com.sequenceiq.cloudbreak.cloud.aws.client.AmazonCloudFormationRetryClient;
import com.sequenceiq.cloudbreak.cloud.aws.connector.resource.AwsResourceConnector;
import com.sequenceiq.cloudbreak.cloud.aws.encryption.EncryptedImageCopyService;
import com.sequenceiq.cloudbreak.cloud.aws.task.AwsCreateStackStatusCheckerTask;
import com.sequenceiq.cloudbreak.cloud.model.CloudStack;
import com.sequenceiq.cloudbreak.cloud.model.Volume;
import com.sequenceiq.cloudbreak.cloud.notification.PersistenceNotifier;
import com.sequenceiq.cloudbreak.common.type.ResourceType;
import com.sequenceiq.cloudbreak.util.FreeMarkerTemplateUtils;

@MockBeans({
        @MockBean(AwsCreateStackStatusCheckerTask.class),
})
public class AwsLauchTest extends AwsComponentTest {

    private static final String AUTOSCALING_GROUP_NAME = "autoscalingGroupName";

    private static final String INSTANCE_ID = "instanceId";

    private static final String VOLUME_ID = "VolumeId";

    private static final String VOLUME_ID_1 = "VolumeId_1";

    private static final String VOLUME_ID_2 = "VolumeId_2";

    private static final int VOLUME_SIZE = 10;

    private static final List<Volume> VOLUMES_TO_CREATE = ImmutableList.of(
            new Volume("/hadoop/fs1", "HDD", VOLUME_SIZE),
            new Volume("/hadoop/fs2", "HDD", VOLUME_SIZE)
    );

    private boolean describeVolumeRequestFirstInvocation = true;

    private int invocationCount;

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
    private AwsCreateStackStatusCheckerTask awsCreateStackStatusCheckerTask;

    @Inject
    private AmazonAutoScalingRetryClient amazonAutoScalingRetryClient;

    @Test
    public void launchStack() throws Exception {
        setupFreemarkerTemplateProcessing();
        setupDescribeStacksResponses();
        setupDescribeImagesResponse();
        setupCreateStackStatusCheckerTask();
        setupDescribeStackResourceResponse();
        setupAutoscalingResponses();
        setupDescribeInstanceStatusResponse();
        setupCreateVolumeResponse();
        setupDescribeVolumeResponse();
        CloudStack stackToBuild = new CloudStackBuilder().withVolumes(VOLUMES_TO_CREATE).build();

        awsResourceConnector.launch(getAuthenticatedContext(), stackToBuild, persistenceNotifier, AdjustmentType.EXACT, Long.MAX_VALUE);

        InOrder inOrderClient = inOrder(amazonEC2Client, amazonCloudFormationRetryClient, awsCreateStackStatusCheckerTask, persistenceNotifier);
        inOrderClient.verify(amazonCloudFormationRetryClient).describeStacks(any());
        inOrderClient.verify(persistenceNotifier).notifyAllocation(
                argThat(cloudResource -> ResourceType.CLOUDFORMATION_STACK.equals(cloudResource.getType())), any());
        inOrderClient.verify(amazonEC2Client).describeImages(any());
        inOrderClient.verify(amazonCloudFormationRetryClient).createStack(any());
        inOrderClient.verify(awsCreateStackStatusCheckerTask).call();
        inOrderClient.verify(persistenceNotifier).notifyAllocation(argThat(cloudResource -> ResourceType.AWS_VPC.equals(cloudResource.getType())), any());
        inOrderClient.verify(persistenceNotifier).notifyAllocation(argThat(cloudResource -> ResourceType.AWS_SUBNET.equals(cloudResource.getType())), any());

        inOrderClient.verify(persistenceNotifier).notifyAllocation(argThat(cloudResource -> ResourceType.AWS_VOLUMESET.equals(cloudResource.getType())), any());
        inOrderClient.verify(amazonEC2Client, times(VOLUMES_TO_CREATE.size())).createVolume(argThat(cv -> cv.getSize() == VOLUME_SIZE));
        inOrderClient.verify(persistenceNotifier).notifyUpdate(argThat(cloudResource -> ResourceType.AWS_VOLUMESET.equals(cloudResource.getType())), any());
        inOrderClient.verify(amazonEC2Client).describeVolumes(argThat(dv -> containsInOrder(dv, VOLUME_ID_1, VOLUME_ID_2)));

        inOrderClient.verify(amazonEC2Client).attachVolume(argThat(x -> x.getVolumeId().equals(VOLUME_ID_1)));
        inOrderClient.verify(amazonEC2Client).attachVolume(argThat(x -> x.getVolumeId().equals(VOLUME_ID_2)));
        inOrderClient.verify(persistenceNotifier).notifyUpdate(argThat(cloudResource -> ResourceType.AWS_VOLUMESET.equals(cloudResource.getType())), any());
        inOrderClient.verify(amazonEC2Client).describeVolumes(argThat(dv -> containsInOrder(dv, VOLUME_ID_1, VOLUME_ID_2)));
    }

    private boolean containsInOrder(DescribeVolumesRequest dv, String value1, String value2) {
        return dv.getVolumeIds().get(0).equals(value1) && dv.getVolumeIds().get(1).equals(value2);
    }

    private void setupFreemarkerTemplateProcessing() throws IOException, freemarker.template.TemplateException {
        when(freeMarkerTemplateUtils.processTemplateIntoString(any(), any())).thenReturn("processedTemplate");
    }

    private void setupCreateVolumeResponse() {
        when(amazonEC2Client.createVolume(any())).thenAnswer(
                new Answer<CreateVolumeResult>() {
                    public CreateVolumeResult answer(InvocationOnMock invocation) {
                        ++invocationCount;
                        return new CreateVolumeResult().withVolume(
                                new com.amazonaws.services.ec2.model.Volume().withVolumeId(VOLUME_ID + '_' + invocationCount)
                        );
                    }
                }
        );
    }

    private void setupDescribeVolumeResponse() {
        when(amazonEC2Client.describeVolumes(any())).thenAnswer(
                (Answer) invocation -> {
                    DescribeVolumesResult describeVolumesResult = new DescribeVolumesResult();
                    Object[] args = invocation.getArguments();
                    DescribeVolumesRequest describeVolumesRequest = (DescribeVolumesRequest) args[0];
                    VolumeState currentVolumeState = getCurrentVolumeState();
                    describeVolumesRequest.getVolumeIds().forEach(
                            volume -> describeVolumesResult.withVolumes(
                                    new com.amazonaws.services.ec2.model.Volume().withState(currentVolumeState)
                            )
                    );
                    return describeVolumesResult;
                }
        );
    }

    private VolumeState getCurrentVolumeState() {
        VolumeState currentVolumeState = describeVolumeRequestFirstInvocation ? VolumeState.Available : VolumeState.InUse;
        describeVolumeRequestFirstInvocation = false;
        return currentVolumeState;
    }

    private void setupDescribeInstanceStatusResponse() {
        when(amazonEC2Client.describeInstanceStatus(any())).thenReturn(
                new DescribeInstanceStatusResult().withInstanceStatuses(
                        new com.amazonaws.services.ec2.model.InstanceStatus().withInstanceState(new InstanceState().withCode(INSTANCE_STATE_RUNNING)))
        );
    }

    private void setupDescribeStacksResponses() {
        when(amazonCloudFormationRetryClient.describeStacks(any()))
                .thenThrow(new AmazonServiceException("stack does not exist"))
                .thenReturn(getDescribeStacksResult())
                .thenReturn(getDescribeStacksResult())
                .thenReturn(getDescribeStacksResult());
    }

    private void setupDescribeImagesResponse() {
        when(amazonEC2Client.describeImages(any())).thenReturn(
                new DescribeImagesResult()
                        .withImages(new com.amazonaws.services.ec2.model.Image().withRootDeviceName(""))
        );
    }

    private void setupCreateStackStatusCheckerTask() {
        when(awsCreateStackStatusCheckerTask.completed(anyBoolean())).thenReturn(true);
        when(awsCreateStackStatusCheckerTask.call()).thenReturn(true);
    }

    private void setupDescribeStackResourceResponse() {
        StackResourceDetail stackResourceDetail = new StackResourceDetail().withPhysicalResourceId(AUTOSCALING_GROUP_NAME);
        DescribeStackResourceResult describeStackResourceResult = new DescribeStackResourceResult().withStackResourceDetail(stackResourceDetail);
        when(amazonCloudFormationRetryClient.describeStackResource(any())).thenReturn(describeStackResourceResult);
    }

    private void setupAutoscalingResponses() {
        DescribeScalingActivitiesResult describeScalingActivitiesResult = new DescribeScalingActivitiesResult();
        when(amazonAutoScalingRetryClient.describeScalingActivities(any())).thenReturn(describeScalingActivitiesResult);

        DescribeAutoScalingGroupsResult describeAutoScalingGroupsResult = new DescribeAutoScalingGroupsResult()
                .withAutoScalingGroups(
                        new AutoScalingGroup()
                                .withInstances(new Instance().withLifecycleState(LifecycleState.InService).withInstanceId(INSTANCE_ID))
                                .withAutoScalingGroupName(AUTOSCALING_GROUP_NAME)
                );
        when(amazonAutoScalingRetryClient.describeAutoScalingGroups(any())).thenReturn(describeAutoScalingGroupsResult);
    }

    private DescribeStacksResult getDescribeStacksResult() {
        return new DescribeStacksResult().withStacks(
                new Stack().withOutputs(
                        new Output().withOutputKey("CreatedVpc").withOutputValue("vpc-id"),
                        new Output().withOutputKey("CreatedSubnet").withOutputValue("subnet-id"),
                        new Output().withOutputKey("EIPAllocationIDmaster1").withOutputValue("eipalloc-id")
                ));
    }
}

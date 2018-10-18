package com.sequenceiq.cloudbreak.controller.validation.stack;

import com.google.common.collect.Sets;
import com.sequenceiq.cloudbreak.api.model.BlueprintRequest;
import com.sequenceiq.cloudbreak.api.model.TemplateRequest;
import com.sequenceiq.cloudbreak.api.model.stack.StackRequest;
import com.sequenceiq.cloudbreak.api.model.stack.cluster.ClusterRequest;
import com.sequenceiq.cloudbreak.api.model.stack.cluster.host.HostGroupRequest;
import com.sequenceiq.cloudbreak.api.model.stack.instance.InstanceGroupRequest;
import com.sequenceiq.cloudbreak.controller.validation.ValidationResult;
import com.sequenceiq.cloudbreak.controller.validation.ValidationResult.State;
import com.sequenceiq.cloudbreak.controller.validation.template.TemplateRequestValidator;
import com.sequenceiq.cloudbreak.domain.Blueprint;
import com.sequenceiq.cloudbreak.domain.json.Json;
import com.sequenceiq.cloudbreak.service.RestRequestThreadLocalService;
import com.sequenceiq.cloudbreak.service.blueprint.BlueprintService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StackRequestValidatorTest {

    private static final Long WORKSPACE_ID = 1L;
    public static final String TEST_BP_NAME = "testBpName";

    @Spy
    private final TemplateRequestValidator templateRequestValidator = new TemplateRequestValidator();

    @Mock
    private BlueprintService blueprintService;

    @Mock
    private RestRequestThreadLocalService restRequestThreadLocalService;

    @InjectMocks
    private StackRequestValidator underTest;

    @Mock
    private Blueprint blueprint;

    @Mock
    private Json blueprintJson;

    @Before
    public void setUp() {
        when(blueprintService.getByNameForWorkspaceId(anyString(), eq(WORKSPACE_ID))).thenReturn(blueprint);
        when(blueprint.getTags()).thenReturn(blueprintJson);
        when(blueprintJson.getMap()).thenReturn(Collections.emptyMap());
        when(restRequestThreadLocalService.getRequestedWorkspaceId()).thenReturn(WORKSPACE_ID);
    }

    @Test
    public void testWithZeroRootVolumeSize() {
        assertNotNull(templateRequestValidator);
        StackRequest stackRequest = stackRequestWithRootVolumeSize(0);
        ValidationResult validationResult = underTest.validate(stackRequest);
        assertEquals(State.ERROR, validationResult.getState());
    }

    @Test
    public void testWithNegativeRootVolumeSize() {
        StackRequest stackRequest = stackRequestWithRootVolumeSize(-1);
        ValidationResult validationResult = underTest.validate(stackRequest);
        assertEquals(State.ERROR, validationResult.getState());
    }

    @Test
    public void testNullValueIsAllowedForRootVolumeSize() {
        StackRequest stackRequest = stackRequestWithRootVolumeSize(null);
        ValidationResult validationResult = underTest.validate(stackRequest);
        assertEquals(State.VALID, validationResult.getState());
    }

    @Test
    public void testWithPositiveRootVolumeSize() {
        StackRequest stackRequest = stackRequestWithRootVolumeSize(1);
        ValidationResult validationResult = underTest.validate(stackRequest);
        assertEquals(State.VALID, validationResult.getState());
    }

    @Test
    public void testWithLargerInstanceGroupSetThanHostGroups() {
        String plusOne = "very master";
        StackRequest stackRequest = stackRequestWithInstanceAndHostGroups(
                Sets.newHashSet(plusOne, "master", "worker", "compute"),
                Sets.newHashSet("master", "worker", "compute")
        );

        ValidationResult validationResult = underTest.validate(stackRequest);
        assertEquals(State.ERROR, validationResult.getState());
        assertTrue(validationResult.getErrors().get(0).startsWith("There are instance groups in the request that do not have a corresponding host group: "
                + plusOne));
    }

    @Test
    public void testWithLargerHostGroupSetThanInstanceGroups() {
        StackRequest stackRequest = stackRequestWithInstanceAndHostGroups(
                Sets.newHashSet("master", "worker", "compute"),
                Sets.newHashSet("super master", "master", "worker", "compute")
        );

        ValidationResult validationResult = underTest.validate(stackRequest);
        assertEquals(State.ERROR, validationResult.getState());
        assertTrue(validationResult.getErrors().get(0).startsWith("There are host groups in the request that do not have a corresponding instance group"));
    }

    @Test
    public void testWithBothGroupContainsDifferentValues() {
        StackRequest stackRequest = stackRequestWithInstanceAndHostGroups(
                Sets.newHashSet("worker", "compute"),
                Sets.newHashSet("master", "worker")
        );

        ValidationResult validationResult = underTest.validate(stackRequest);
        assertEquals(State.ERROR, validationResult.getState());
        assertEquals(2L, validationResult.getErrors().size());
    }

    private StackRequest stackRequestWithInstanceAndHostGroups(Set<String> instanceGroups, Set<String> hostGroups) {
        List<InstanceGroupRequest> instanceGroupList = instanceGroups.stream()
                .map(ig -> getInstanceGroupRequest(new TemplateRequest(), ig))
                .collect(Collectors.toList());

        Set<HostGroupRequest> hostGroupSet = hostGroups.stream()
                .map(hg -> {
                    HostGroupRequest hostGroupRequest = new HostGroupRequest();
                    hostGroupRequest.setName(hg);
                    return hostGroupRequest;
                })
                .collect(Collectors.toSet());

        ClusterRequest clusterRequest = new ClusterRequest();
        clusterRequest.setHostGroups(hostGroupSet);
        BlueprintRequest bpRequest = new BlueprintRequest();
        bpRequest.setName(TEST_BP_NAME);
        clusterRequest.setBlueprint(bpRequest);
        clusterRequest.setBlueprintName(TEST_BP_NAME);
        return getStackRequest(instanceGroupList, clusterRequest);
    }

    private StackRequest stackRequestWithRootVolumeSize(Integer rootVolumeSize) {
        TemplateRequest templateRequest = new TemplateRequest();
        templateRequest.setRootVolumeSize(rootVolumeSize);
        InstanceGroupRequest instanceGroupRequest = getInstanceGroupRequest(templateRequest, "master");
        ClusterRequest clusterRequest = getClusterRequest();
        return getStackRequest(Collections.singletonList(instanceGroupRequest), clusterRequest);
    }

    private InstanceGroupRequest getInstanceGroupRequest(TemplateRequest templateRequest, String master) {
        InstanceGroupRequest instanceGroupRequest = new InstanceGroupRequest();
        instanceGroupRequest.setGroup(master);
        instanceGroupRequest.setTemplate(templateRequest);
        return instanceGroupRequest;
    }

    private ClusterRequest getClusterRequest() {
        HostGroupRequest hostGroupRequest = new HostGroupRequest();
        ClusterRequest clusterRequest = new ClusterRequest();
        hostGroupRequest.setName("master");
        clusterRequest.setHostGroups(Sets.newHashSet(hostGroupRequest));
        BlueprintRequest bpRequest = new BlueprintRequest();
        bpRequest.setName(TEST_BP_NAME);
        clusterRequest.setBlueprint(bpRequest);
        clusterRequest.setBlueprintName(TEST_BP_NAME);
        return clusterRequest;
    }

    private StackRequest getStackRequest(List<InstanceGroupRequest> instanceGroupRequests, ClusterRequest clusterRequest) {
        StackRequest stackRequest = new StackRequest();
        stackRequest.setClusterRequest(clusterRequest);
        stackRequest.setInstanceGroups(instanceGroupRequests);
        return stackRequest;
    }

}
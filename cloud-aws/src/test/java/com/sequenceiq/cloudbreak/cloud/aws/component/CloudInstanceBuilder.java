package com.sequenceiq.cloudbreak.cloud.aws.component;

import static com.sequenceiq.cloudbreak.cloud.aws.TestConstants.GROUP_NAME_MASTER;
import static com.sequenceiq.cloudbreak.cloud.model.InstanceStatus.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sequenceiq.cloudbreak.cloud.model.CloudInstance;
import com.sequenceiq.cloudbreak.cloud.model.InstanceAuthentication;
import com.sequenceiq.cloudbreak.cloud.model.InstanceStatus;
import com.sequenceiq.cloudbreak.cloud.model.InstanceTemplate;
import com.sequenceiq.cloudbreak.cloud.model.Volume;

public class CloudInstanceBuilder {

    private static final int VOLUME_SIZE = 10;

    private static int nextId = 0;

    private InstanceAuthentication instanceAuthentication = new InstanceAuthenticationBuilder().build();

    private List<Volume> volumes = new ArrayList<>();

    private InstanceStatus instanceStatus = CREATE_REQUESTED;

    private final String groupName = GROUP_NAME_MASTER;

    private String instanceId;

    public CloudInstanceBuilder withVolumes(List<Volume> volumes) {
        this.volumes.addAll(volumes);
        return this;
    }

    public CloudInstanceBuilder withInstanceStatus(InstanceStatus instanceStatus) {
        this.instanceStatus = instanceStatus;
        return this;
    }

    public CloudInstanceBuilder withInstanceId(String instanceId) {
        this.instanceId = instanceId;
        return this;
    }

    public CloudInstance build() {
        if (volumes.isEmpty()) {
            volumes = getDefaultVolumes();
        }

        checkInstanceId(instanceStatus);

        InstanceTemplate instanceTemplate = new InstanceTemplate(
                "m1.medium",
                groupName,
                0L,
                volumes,
                instanceStatus,
                new HashMap<>(),
                0L,
                "cb-centos66-amb200-2015-05-25"
        );
        Map<String, Object> params = new HashMap<>();
        return new CloudInstance(instanceId, instanceTemplate, instanceAuthentication, params);
    }

    private List<Volume> getDefaultVolumes() {
        return List.of(
                new Volume("/hadoop/fs1", "HDD", VOLUME_SIZE),
                new Volume("/hadoop/fs2", "HDD", VOLUME_SIZE)
        );
    }

    private static String getNextInstanceId() {
        String instanceId = Integer.toString(nextId++);
        return instanceId;
    }

    private void checkInstanceId(InstanceStatus instanceStatus) {
        instanceId = !CREATE_REQUESTED.equals(instanceStatus) && instanceId == null ? getNextInstanceId() : null;
    }
}

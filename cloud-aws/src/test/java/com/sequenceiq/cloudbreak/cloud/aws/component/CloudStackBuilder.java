package com.sequenceiq.cloudbreak.cloud.aws.component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.sequenceiq.cloudbreak.api.model.stack.instance.InstanceGroupType;
import com.sequenceiq.cloudbreak.cloud.model.CloudStack;
import com.sequenceiq.cloudbreak.cloud.model.Group;
import com.sequenceiq.cloudbreak.cloud.model.Image;
import com.sequenceiq.cloudbreak.cloud.model.InstanceAuthentication;
import com.sequenceiq.cloudbreak.cloud.model.Network;
import com.sequenceiq.cloudbreak.cloud.model.Subnet;
import com.sequenceiq.cloudbreak.cloud.model.Volume;

public class CloudStackBuilder {

    private static final String LOGIN_USER_NAME = "loginusername";

    private static final String PUBLIC_KEY = "pubkey";

    private static final int ROOT_VOLUME_SIZE = 50;

    private static final String CORE_CUSTOM_DATA = "CORE";

    private static final String GATEWAY_CUSTOM_DATA = "GATEWAY";

    private static final String CIDR = "10.10.10.10/16";

    private List<Volume> volumes;

    private List<Group> groups = new ArrayList<>();

    public CloudStackBuilder withVolumes(List<Volume> volumes) {
        this.volumes = volumes;
        return this;
    }

    public CloudStackBuilder withGroup(Group group) {
        groups.add(group);
        return this;
    }

    public CloudStack build() throws IOException {
        Network network = new Network(new Subnet(CIDR));
        Map<InstanceGroupType, String> userData = ImmutableMap.of(
                InstanceGroupType.CORE, CORE_CUSTOM_DATA,
                InstanceGroupType.GATEWAY, GATEWAY_CUSTOM_DATA
        );
        Image image = new Image("cb-centos66-amb200-2015-05-25", userData, "redhat6", "redhat6", "", "default", "default-id", new HashMap<>());
        String template = "stack_template";

        if (groups.isEmpty()) {
            groups.add(new GroupBuilder().build());
        }
        return new CloudStack(
                groups, network, image, Map.of(), Map.of(), template, getDefaultInstanceAuthentication(), LOGIN_USER_NAME, PUBLIC_KEY, null
        );
    }

    private InstanceAuthentication getDefaultInstanceAuthentication() {
        return new InstanceAuthentication(PUBLIC_KEY, "pubkeyid", LOGIN_USER_NAME);
    }

}

package com.sequenceiq.cloudbreak.cloud.aws.component;

import static com.sequenceiq.cloudbreak.cloud.aws.TestConstants.GROUP_NAME_MASTER;
import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sequenceiq.cloudbreak.api.model.stack.instance.InstanceGroupType;
import com.sequenceiq.cloudbreak.cloud.model.CloudInstance;
import com.sequenceiq.cloudbreak.cloud.model.Group;
import com.sequenceiq.cloudbreak.cloud.model.InstanceAuthentication;
import com.sequenceiq.cloudbreak.cloud.model.PortDefinition;
import com.sequenceiq.cloudbreak.cloud.model.Security;
import com.sequenceiq.cloudbreak.cloud.model.SecurityRule;

public class GroupBuilder {

    private static final int ROOT_VOLUME_SIZE = 50;


    private InstanceAuthentication instanceAuthentication = new InstanceAuthenticationBuilder().build();

    private Security security = getDefaultSecurity();

    private List<CloudInstance> instances = new ArrayList<>();

    private String groupNameMaster = GROUP_NAME_MASTER;

    public GroupBuilder withInstanceAuthentication(InstanceAuthentication instanceAuthentication) {
        this.instanceAuthentication = instanceAuthentication;
        return this;
    }

    public GroupBuilder withInstance(CloudInstance cloudInstance) {
        instances.add(cloudInstance);
        return this;
    }

    public Group build() {
        if (instances.isEmpty()) {
            instances.add(new CloudInstanceBuilder().build());
        }

        return new Group(
                groupNameMaster,
                InstanceGroupType.CORE,
                instances,
                security,
                null,
                instanceAuthentication,
                instanceAuthentication.getLoginUserName(),
                instanceAuthentication.getPublicKey(),
                ROOT_VOLUME_SIZE
        );
    }

    private Security getDefaultSecurity() {
        List<SecurityRule> rules = Collections.singletonList(new SecurityRule("0.0.0.0/0",
                new PortDefinition[]{new PortDefinition("22", "22"), new PortDefinition("443", "443")}, "tcp"));
        return new Security(rules, emptyList());
    }
}


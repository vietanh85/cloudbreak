package com.sequenceiq.it.cloudbreak.newway.action;

import java.util.Map;

import javax.inject.Inject;

import com.sequenceiq.it.cloudbreak.newway.ApplicationContextProvider;
import com.sequenceiq.it.cloudbreak.newway.CloudbreakClient;
import com.sequenceiq.it.cloudbreak.newway.Prototype;
import com.sequenceiq.it.cloudbreak.newway.StackEntity;
import com.sequenceiq.it.cloudbreak.newway.assertion.AssertionV2;
import com.sequenceiq.it.cloudbreak.newway.context.TestContext;
import com.sequenceiq.it.cloudbreak.newway.wait.WaitUtil;

@Prototype
public class WaitAndCheckClusterDeletedAction implements AssertionV2<StackEntity> {

    @Inject
    private WaitUtil waitUtil;

    @Override
    public StackEntity doAssertion(TestContext testContext, StackEntity entity, CloudbreakClient client) throws Exception {
        waitUtil.waitAndCheckStatuses(client, entity.getName(), Map.of("status", "DELETE_COMPLETED"));
        return entity;
    }

    public static AssertionV2<StackEntity> create() {
        return ApplicationContextProvider.getBean(WaitAndCheckClusterDeletedAction.class);
    }
}

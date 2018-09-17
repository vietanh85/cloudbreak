package com.sequenceiq.it.cloudbreak.newway.testcase;


import static com.sequenceiq.it.cloudbreak.newway.context.RunningParameter.force;
import static com.sequenceiq.it.cloudbreak.newway.context.RunningParameter.key;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sequenceiq.it.cloudbreak.newway.Stack;
import com.sequenceiq.it.cloudbreak.newway.StackEntity;
import com.sequenceiq.it.cloudbreak.newway.action.StackRefreshAction;
import com.sequenceiq.it.cloudbreak.newway.context.TestContext;
import com.sequenceiq.it.cloudbreak.newway.entity.AmbariEntity;
import com.sequenceiq.it.cloudbreak.newway.entity.ClusterEntity;
import com.sequenceiq.it.cloudbreak.newway.v3.StackV3Action;

public class TerminationTest extends AbstractIntegrationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(TerminationTest.class);

    @BeforeMethod
    public void beforeMethod(Object[] data) {
        TestContext testContext = (TestContext) data[0];
        createDefaultUser(testContext);
        createDefaultCredential(testContext);
        createDefaultImageCatalog(testContext);
        testContext
                // create stack
                .given(StackEntity.class)
                .when(Stack.postV2())
                .await(STACK_AVAILABLE);

    }

    @AfterMethod(alwaysRun = true)
    public void tear(Object[] data) {
        TestContext testContext = (TestContext) data[0];
        testContext.cleanupTestContextEntity();

        if (!testContext.getErrors().isEmpty()) {
            testContext.getErrors().forEach(LOGGER::error);
            Assert.fail("See exceptions below");
        }
    }

    @Test(dataProvider = "testContext")
    public void testInstanceTermination(TestContext testContext) {
        String blueprintName = "Data Science: Apache Spark 2, Apache Zeppelin";
        String clusterName = getNameGenerator().getRandomNameForMock();
        testContext
                .given(StackEntity.class)
                //select an instance id
                .select(s -> s.getInstanceId("worker"), key("instanceId"))
                .capture(s -> s.getInstanceMetaData("worker").size() - 1, key("metadatasize"))
                .when(Stack::deleteInstance)
                .await(STACK_AVAILABLE)
                .verify(s -> s.getInstanceMetaData("worker").size(), key("metadatasize"))
                .validate();
    }

    @Test(dataProvider = "testContext", enabled = false)
    public void testInstanceTermination2(TestContext testContext) {
        String blueprintName = "Data Science: Apache Spark 2, Apache Zeppelin";
        String clusterName = "mockcluster";
        testContext.given(ClusterEntity.class).withName(clusterName)
                .given(AmbariEntity.class).withBlueprintName(blueprintName)
                .given(StackEntity.class).withName(clusterName).withGatewayPort(testContext.getSparkServer().getPort())
                .when(Stack.postV2())
                .then(Stack::waitAndCheckClusterAndStackAvailabilityStatusV2);


        String hostGroupName = "worker";
        StackEntity stack = testContext.get(StackEntity.class);
        testContext.when(stack, new StackRefreshAction());
        int before = stack.getInstanceMetaData(hostGroupName).size();

        stack.when(Stack.deleteInstance(stack.getInstanceId(hostGroupName)))
                .then(Stack::waitAndCheckClusterAndStackAvailabilityStatusV2);

        stack.when(new StackRefreshAction());
        int after = stack.getInstanceMetaData(hostGroupName).size();

        stack.when(StackEntity.class, StackV3Action::deleteV2, force())
                .then(Stack::waitAndCheckClusterDeletedV2, force());

        Assert.assertEquals(after, before - 1);
    }
}

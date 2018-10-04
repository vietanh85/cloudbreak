package com.sequenceiq.it.cloudbreak.newway.testcase;

import static com.sequenceiq.it.cloudbreak.newway.context.RunningParameter.force;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sequenceiq.it.cloudbreak.newway.Stack;
import com.sequenceiq.it.cloudbreak.newway.StackEntity;
import com.sequenceiq.it.cloudbreak.newway.action.WaitAndCheckClusterDeletedAction;
import com.sequenceiq.it.cloudbreak.newway.context.TestContext;
import com.sequenceiq.it.cloudbreak.newway.v3.StackV3Action;

public class StackCreationTest extends AbstractIntegrationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(StackCreationTest.class);

    @BeforeMethod
    public void beforeMethod(Object[] data) {
        TestContext testContext = (TestContext) data[0];

        createDefaultUser(testContext);
        createDefaultCredential(testContext);
        createDefaultImageCatalog(testContext);
    }

    @Test(dataProvider = "testContext")
    public void testCreateNewRegularCluster(TestContext testContext) {
        testContext.given(StackEntity.class)
                .when(Stack.postV2())
                .await(STACK_AVAILABLE)
                .when(StackEntity.class, StackV3Action::deleteV2, force())
                .then(WaitAndCheckClusterDeletedAction.create())
                .validate();
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
}

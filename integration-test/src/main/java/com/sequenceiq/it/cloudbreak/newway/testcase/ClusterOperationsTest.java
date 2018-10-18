package com.sequenceiq.it.cloudbreak.newway.testcase;

import com.sequenceiq.it.cloudbreak.newway.RandomNameCreator;
import com.sequenceiq.it.cloudbreak.newway.Stack;
import com.sequenceiq.it.cloudbreak.newway.StackEntity;
import com.sequenceiq.it.cloudbreak.newway.context.TestContext;
import com.sequenceiq.it.spark.StatefulRoute;
import com.sequenceiq.it.spark.spi.CloudVmInstanceStatuses;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import spark.Route;

import javax.inject.Inject;

import static com.sequenceiq.it.cloudbreak.newway.Mock.gson;
import static java.lang.String.format;

public class ClusterOperationsTest extends AbstractIntegrationTest {

    private static final String TEST_CONTEXT = "testContext";

    @Inject
    private RandomNameCreator creator;

    @BeforeMethod
    public void beforeMethod(Object[] data) {
        TestContext testContext = (TestContext) data[0];
        createDefaultUser(testContext);
        createDefaultCredential(testContext);
        createDefaultImageCatalog(testContext);
    }

    @AfterMethod(alwaysRun = true)
    public void tear(Object[] data) {
        ((TestContext) data[0]).cleanupTestContextEntity();
    }

    @Test(dataProvider = TEST_CONTEXT)
    public void testClusterStop(TestContext testContext) {
        String clusterName = creator.getRandomNameForMock();
        mockAmbari(testContext, clusterName);
        mockSpi(testContext, clusterName);
        testContext
                .given(StackEntity.class).valid().withName(clusterName)
                .when(Stack.postV2())
                .await(STACK_AVAILABLE)
                .when(Stack.stopV2())
                .await(STACK_STOPPED)
                .validate();
    }

    private void mockAmbari(TestContext testContext, String clusterName) {
        Route passAmbari = (request, response) -> {
            response.type("text/plain");
            response.status(200);
            response.body(format("{\"href\":\"%s\",\"Requests\":{\"id\":12,\"status\":\"Accepted\"}}", request.url()));
            return "";
        };

        testContext.getModel().getAmbariMock().getDynamicRouteStack().clearPost(format("/api/v1/clusters/%s/*", clusterName));
        testContext.getModel().getAmbariMock().getDynamicRouteStack().put(format("/api/v1/clusters/%s/*", clusterName), passAmbari);
    }

    private void mockSpi(TestContext testContext, String clusterName) {
        StatefulRoute okState = (request, response, model) -> {
            String resultJson = gson().toJson(new CloudVmInstanceStatuses(model.getInstanceMap()).createCloudVmInstanceStatuses());
            response.body(resultJson);
            response.type("text/plain");
            response.status(200);
            return "";
        };

        StatefulRoute stoppedStateSpi = (request, response, model) -> {
            String resultJson = gson().toJson(new CloudVmInstanceStatuses(model.getInstanceMap()).createCloudVmInstanceStatuses());
            response.body(resultJson.replaceAll("STARTED", "STOPPED"));
            response.type("text/plain");
            response.status(200);
            return "";
        };

        testContext.getModel().getSpiMock().getDynamicRouteStack().clearPost("/spi/cloud_instance_statuses");
        testContext.getModel().getSpiMock().getDynamicRouteStack().post("/spi/cloud_instance_statuses", okState);
        testContext.getModel().getSpiMock().getDynamicRouteStack().post("/spi/cloud_instance_statuses", stoppedStateSpi);
    }

}

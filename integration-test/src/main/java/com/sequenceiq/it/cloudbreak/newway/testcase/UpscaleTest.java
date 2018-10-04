package com.sequenceiq.it.cloudbreak.newway.testcase;


import static com.sequenceiq.it.cloudbreak.newway.mock.model.AmbariMock.BLUEPRINTS;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sequenceiq.it.cloudbreak.newway.CredentialEntity;
import com.sequenceiq.it.cloudbreak.newway.ImageCatalog;
import com.sequenceiq.it.cloudbreak.newway.Stack;
import com.sequenceiq.it.cloudbreak.newway.StackEntity;
import com.sequenceiq.it.cloudbreak.newway.action.CredentialCreateAction;
import com.sequenceiq.it.cloudbreak.newway.action.ImageCatalogCreateIfNotExistsAction;
import com.sequenceiq.it.cloudbreak.newway.action.StackScalePostAction;
import com.sequenceiq.it.cloudbreak.newway.actor.Actor;
import com.sequenceiq.it.cloudbreak.newway.assertion.MockVerification;
import com.sequenceiq.it.cloudbreak.newway.context.TestContext;
import com.sequenceiq.it.cloudbreak.newway.entity.AmbariEntity;
import com.sequenceiq.it.cloudbreak.newway.entity.ClusterEntity;

import spark.Route;

public class UpscaleTest extends AbstractIntegrationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpscaleTest.class);

    @BeforeMethod
    public void beforeMethod(Object[] data) {
        TestContext testContext = (TestContext) data[0];
        LOGGER.info("All routes added: {}", testContext.getSparkServer().getSparkService().getPaths());
        testContext.as(Actor::defaultUser);
        testContext.given(ImageCatalog.class).withUrl(testContext.getImgCatalog().getImgCatalogUrl())
                .when(new ImageCatalogCreateIfNotExistsAction())
                .when(ImageCatalog::putSetDefaultByName)
                .given(CredentialEntity.class).withParameters(Map.of("mockEndpoint", testContext.getSparkServer().getEndpoint()))
                .when(new CredentialCreateAction());
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
    public void testStackScaling(TestContext testContext) throws Exception {
        // GIVEN
        String blueprintName = "Data Science: Apache Spark 2, Apache Zeppelin";
        String clusterName = getNameGenerator().getRandomNameForMock();
        testContext.given(ClusterEntity.class).withName(clusterName)
                .given(AmbariEntity.class).withBlueprintName(blueprintName)
                .given(StackEntity.class).withName(clusterName).withGatewayPort(testContext.getSparkServer().getPort())
                .when(Stack.postV2())
                .await(STACK_AVAILABLE)
                .when(StackScalePostAction.valid().withDesiredCount(15))
                .await(STACK_AVAILABLE)
                .when(StackScalePostAction.valid().withDesiredCount(6))
                .await(STACK_AVAILABLE)
                .validate();
    }

    @Test(dataProvider = "testContext")
    public void testUpscale(TestContext testContext) {
        String blueprintName = "Data Science: Apache Spark 2, Apache Zeppelin";
        String clusterName = getNameGenerator().getRandomNameForMock();
        testContext.given(ClusterEntity.class).withName(clusterName)
                .given(AmbariEntity.class).withBlueprintName(blueprintName)
                .given(StackEntity.class).withName(clusterName).withGatewayPort(testContext.getSparkServer().getPort())
                .when(Stack.postV2())
                .await(STACK_AVAILABLE)
                .when(StackScalePostAction.valid().withDesiredCount(15))
                .await(StackEntity.class, STACK_AVAILABLE)
                .then(MockVerification.verify(HttpMethod.POST, "/api/v1/blueprints/"))
                .validate();
    }

    @Test(dataProvider = "testContext")
    public void testAmbariFailure(TestContext testContext) {
        mockAmbariBlueprintFail(testContext);
        String blueprintName = "Data Science: Apache Spark 2, Apache Zeppelin";
        String clusterName = getNameGenerator().getRandomNameForMock();
        testContext.given(ClusterEntity.class).withName(clusterName)
                .given(AmbariEntity.class).withBlueprintName(blueprintName)
                .given(StackEntity.class).withName(clusterName).withGatewayPort(testContext.getSparkServer().getPort())
                .when(Stack.postV2())
                .await(STACK_FAILED)
                .then(MockVerification.verify(HttpMethod.POST, "/api/v1/blueprints/").atLeast(1))
                .validate();
    }

    private void mockAmbariBlueprintFail(TestContext testContext) {
        Route customResponse2 = (request, response) -> {
            response.type("text/plain");
            response.status(400);
            response.body("Bad blueprint format");
            return response;
        };
        testContext.getModel().getAmbariMock().getDynamicRouteStack().clearPost(BLUEPRINTS);
        testContext.getModel().getAmbariMock().getDynamicRouteStack().post(BLUEPRINTS, customResponse2);
    }

}

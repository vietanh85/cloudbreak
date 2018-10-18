package com.sequenceiq.it.cloudbreak.newway.testcase;

import static com.sequenceiq.it.cloudbreak.newway.context.RunningParameter.key;

import javax.ws.rs.ForbiddenException;

import com.sequenceiq.it.cloudbreak.newway.Blueprint;
import com.sequenceiq.it.cloudbreak.newway.BlueprintEntity;
import com.sequenceiq.it.cloudbreak.newway.ImageCatalog;
import com.sequenceiq.it.cloudbreak.newway.ImageCatalogEntity;
import com.sequenceiq.it.cloudbreak.newway.LdapConfig;
import com.sequenceiq.it.cloudbreak.newway.LdapConfigEntity;
import com.sequenceiq.it.cloudbreak.newway.ProxyConfig;
import com.sequenceiq.it.cloudbreak.newway.ProxyConfigEntity;
import com.sequenceiq.it.cloudbreak.newway.Recipe;
import com.sequenceiq.it.cloudbreak.newway.RecipeEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sequenceiq.it.cloudbreak.newway.CloudbreakTest;
import com.sequenceiq.it.cloudbreak.newway.Stack;
import com.sequenceiq.it.cloudbreak.newway.StackEntity;
import com.sequenceiq.it.cloudbreak.newway.context.TestContext;

public class WorkspaceTest extends AbstractIntegrationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkspaceTest.class);

    private static final String FORBIDDEN_KEY = "forbiddenGetByName";

    @BeforeMethod
    public void beforeMethod(Object[] data) {
        TestContext testContext = (TestContext) data[0];

        createDefaultUser(testContext);
        createSecondUser(testContext);
        createDefaultCredential(testContext);
        createDefaultImageCatalog(testContext);

        testContext.given(StackEntity.class)
                .when(Stack.postV2())
                .await(STACK_AVAILABLE);
    }

    @AfterMethod(alwaysRun = true)
    public void tear(Object[] data) {
        TestContext testContext = (TestContext) data[0];
        testContext.cleanupTestContextEntity();
    }

    @Test(dataProvider = "testContext", enabled = false)
    public void testCreateAStackAndGetOtherUser(TestContext testContext) {
        testContext
                .given(StackEntity.class)
                .when(Stack::getByName, key(FORBIDDEN_KEY).withWho(CloudbreakTest.SECOND_USER).withLogError(false))
                .except(ForbiddenException.class, key(FORBIDDEN_KEY))
                .validate();
    }

    @Test(dataProvider = "testContext", enabled = false)
    public void testCreateABlueprintAndGetOtherUser(TestContext testContext) {
        testContext
                .given(BlueprintEntity.class)
                .when(Blueprint::getByName, key(FORBIDDEN_KEY).withWho(CloudbreakTest.SECOND_USER).withLogError(false))
                .except(ForbiddenException.class, key(FORBIDDEN_KEY))
                .validate();
    }

    @Test(dataProvider = "testContext", enabled = false)
    public void testCreateARecipeAndGetOtherUser(TestContext testContext) {
        testContext
                .given(RecipeEntity.class)
                .when(Recipe::getByName, key(FORBIDDEN_KEY).withWho(CloudbreakTest.SECOND_USER).withLogError(false))
                .except(ForbiddenException.class, key(FORBIDDEN_KEY))
                .validate();
    }

    @Test(dataProvider = "testContext", enabled = false)
    public void testCreateAnLdapAndGetOtherUser(TestContext testContext) {
        testContext
                .given(LdapConfigEntity.class)
                .when(LdapConfig::getByName, key(FORBIDDEN_KEY).withWho(CloudbreakTest.SECOND_USER).withLogError(false))
                .except(ForbiddenException.class, key(FORBIDDEN_KEY))
                .validate();
    }

    @Test(dataProvider = "testContext", enabled = false)
    public void testCreateAnImageCatalogWithImagesAndGetOtherUser(TestContext testContext) {
        testContext
                .given(ImageCatalogEntity.class)
                .when(ImageCatalog::getByNameAndImages, key(FORBIDDEN_KEY).withWho(CloudbreakTest.SECOND_USER).withLogError(false))
                .except(ForbiddenException.class, key(FORBIDDEN_KEY))
                .validate();
    }

    @Test(dataProvider = "testContext", enabled = false)
    public void testCreateAnImageCatalogWithoutImagesAndGetOtherUser(TestContext testContext) {
        testContext
                .given(ImageCatalogEntity.class)
                .when(ImageCatalog::getByNameWithoutImages, key(FORBIDDEN_KEY).withWho(CloudbreakTest.SECOND_USER).withLogError(false))
                .except(ForbiddenException.class, key(FORBIDDEN_KEY))
                .validate();
    }

    @Test(dataProvider = "testContext", enabled = false)
    public void testCreateAProxyConfigAndGetOtherUser(TestContext testContext) {
        testContext
                .given(ProxyConfigEntity.class)
                .when(ProxyConfig::getByName, key(FORBIDDEN_KEY).withWho(CloudbreakTest.SECOND_USER).withLogError(false))
                .except(ForbiddenException.class, key(FORBIDDEN_KEY))
                .validate();
    }

}

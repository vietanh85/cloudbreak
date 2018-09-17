package com.sequenceiq.it.cloudbreak.newway.testcase;

import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;

import com.sequenceiq.it.cloudbreak.newway.ApplicationContextProvider;
import com.sequenceiq.it.cloudbreak.newway.CredentialEntity;
import com.sequenceiq.it.cloudbreak.newway.ImageCatalog;
import com.sequenceiq.it.cloudbreak.newway.ImageCatalogEntity;
import com.sequenceiq.it.cloudbreak.newway.RandomNameCreator;
import com.sequenceiq.it.cloudbreak.newway.action.CredentialCreateAction;
import com.sequenceiq.it.cloudbreak.newway.action.ImageCatalogCreateIfNotExistsAction;
import com.sequenceiq.it.cloudbreak.newway.actor.Actor;
import com.sequenceiq.it.cloudbreak.newway.context.PurgeGarbageService;
import com.sequenceiq.it.cloudbreak.newway.context.TestContext;
import com.sequenceiq.it.cloudbreak.newway.mock.MockPoolConfiguration;
import com.sequenceiq.it.config.IntegrationTestConfiguration;

@ContextConfiguration(classes = {IntegrationTestConfiguration.class, MockPoolConfiguration.class}, initializers = ConfigFileApplicationContextInitializer.class)
public abstract class AbstractIntegrationTest extends AbstractTestNGSpringContextTests {

    public static final Map<String, String> STACK_DELETED = Map.of("status", "DELETE_COMPLETED");

    protected static final Map<String, String> STACK_AVAILABLE = Map.of("status", "AVAILABLE", "clusterStatus", "AVAILABLE");

    protected static final Map<String, String> STACK_FAILED = Map.of("status", "AVAILABLE", "clusterStatus", "CREATE_FAILED");

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractIntegrationTest.class);

    @Inject
    private RandomNameCreator nameGenerator;

    @Inject
    private PurgeGarbageService purgeGarbageService;

    @BeforeSuite
    public void beforeSuite(ITestContext testngContext) {

    }

    @BeforeClass
    public void createSharedObjects() {

    }

    @AfterClass(alwaysRun = true)
    public void cleanSharedObjects() {
        purgeGarbageService.purge();
    }

    @AfterSuite
    public void afterSuite() {

    }

    @DataProvider
    public Object[][] testContext() {
        return new Object[][]{{ApplicationContextProvider.getBean(TestContext.class)}};
    }

    public RandomNameCreator getNameGenerator() {
        return nameGenerator;
    }

    protected void createDefaultCredential(TestContext testContext) {
        testContext.given(CredentialEntity.class)
                .when(new CredentialCreateAction());
    }

    protected void createDefaultImageCatalog(TestContext testContext) {
        testContext
                .given(ImageCatalog.class)
                .when(new ImageCatalogCreateIfNotExistsAction())
                .when(ImageCatalogEntity::putSetDefaultByName);
    }

    protected void createDefaultUser(TestContext testContext) {
        testContext.as();
    }

    protected void createSecondUser(TestContext testContext) {
        testContext.as(Actor::secondUser);
    }
}

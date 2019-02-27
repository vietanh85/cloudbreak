package com.sequenceiq.it.cloudbreak.newway.testcase.mock;

import static com.sequenceiq.it.cloudbreak.newway.context.RunningParameter.key;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.ForbiddenException;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.sequenceiq.it.cloudbreak.newway.action.accessconfig.PlatformAccessConfigsTestAction;
import com.sequenceiq.it.cloudbreak.newway.action.credential.CredentialTestAction;
import com.sequenceiq.it.cloudbreak.newway.context.Description;
import com.sequenceiq.it.cloudbreak.newway.context.MockedTestContext;
import com.sequenceiq.it.cloudbreak.newway.context.TestCaseDescription;
import com.sequenceiq.it.cloudbreak.newway.context.TestCaseDescription.TestCaseDescriptionBuilder;
import com.sequenceiq.it.cloudbreak.newway.context.TestContext;
import com.sequenceiq.it.cloudbreak.newway.entity.accessconfig.PlatformAccessConfigsTestDto;
import com.sequenceiq.it.cloudbreak.newway.entity.credential.CredentialTestDto;
import com.sequenceiq.it.cloudbreak.newway.testcase.AbstractIntegrationTest;

public class AccessConfigsTest extends AbstractIntegrationTest {

    @BeforeMethod
    public void beforeMethod(Object[] data) {
        createDefaultUser((TestContext) data[0]);
    }

    @Test(dataProvider = TEST_CONTEXT_WITH_MOCK)
    @Description(
            given = "using a valid MOCK credential",
            when = "calling get access config",
            then = "returns with valid access config for MOCK")
    public void testGetAccessConfigsByCredentialName(MockedTestContext testContext) {
        String credentialName = getNameGenerator().getRandomNameForResource();
        testContext
                .given(CredentialTestDto.class)
                .withName(credentialName)
                .when(CredentialTestAction::create)
                .given(PlatformAccessConfigsTestDto.class)
                .withCredentialName(credentialName)
                .when(PlatformAccessConfigsTestAction::getAccessConfigs)
                .validate();
    }

    @Test(dataProvider = "contextWithCredentialNameAndException")
    public void testGetAccessConfigsByCredentialNameWhenCredentialIsInvalid(
            MockedTestContext testContext,
            String credentialName,
            String exceptionKey,
            Class<Exception> exception,
            @Description TestCaseDescription description) {
        testContext
                .given(PlatformAccessConfigsTestDto.class)
                .withCredentialName(credentialName)
                .when(PlatformAccessConfigsTestAction::getAccessConfigs, key(exceptionKey))
                .expect(exception, key(exceptionKey))
                .validate();
    }

    @DataProvider(name = "contextWithCredentialNameAndException")
    public Object[][] provideInvalidAttributes() {
        return new Object[][]{
                {getBean(MockedTestContext.class), "", "badRequest", BadRequestException.class,
                        new TestCaseDescriptionBuilder()
                        .given("using a empty string as MOCK credential name")
                        .when("calling get access config")
                        .then("returns with BadRequestException for MOCK")
                },
                {getBean(MockedTestContext.class), null, "badRequest", BadRequestException.class,
                        new TestCaseDescriptionBuilder()
                        .given("using a 'null' as MOCK credential name")
                        .when("calling get access config")
                        .then("returns with BadRequestException for MOCK")
                },
                {getBean(MockedTestContext.class), "andNowForSomethingCompletelyDifferent", "forbidden", ForbiddenException.class,
                        new TestCaseDescriptionBuilder()
                        .given("using a not existing name for MOCK credential")
                        .when("calling get access config")
                        .then("returns with ForbiddenException for MOCK")}
        };
    }

}

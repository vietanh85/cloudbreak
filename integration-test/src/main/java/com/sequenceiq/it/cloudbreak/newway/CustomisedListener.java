package com.sequenceiq.it.cloudbreak.newway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class CustomisedListener implements ITestListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomisedListener.class);

    @Override
    public void onTestStart(ITestResult result) {

    }

    @Override
    public void onTestSuccess(ITestResult result) {

    }

    @Override
    public void onTestFailure(ITestResult result) {

    }

    @Override
    public void onTestSkipped(ITestResult result) {

    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {

    }

    @Override
    public void onStart(ITestContext context) {

    }

    // ...
    @Override
    public void onFinish(ITestContext testContext) {
        LOGGER.info("PASSED TEST CASES");
        testContext.getPassedTests().getAllResults()
                .forEach(result -> {LOGGER.info(result.getName());});

        LOGGER.info("FAILED TEST CASES");
        testContext.getFailedTests().getAllResults()
                .forEach(result -> {LOGGER.info(result.getName());});

        LOGGER.info(
                "Test completed on: " + testContext.getEndDate().toString());
    }

    //...
}
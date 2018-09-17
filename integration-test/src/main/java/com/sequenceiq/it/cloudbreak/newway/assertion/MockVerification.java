package com.sequenceiq.it.cloudbreak.newway.assertion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;

import com.sequenceiq.it.cloudbreak.newway.CloudbreakClient;
import com.sequenceiq.it.cloudbreak.newway.StackEntity;
import com.sequenceiq.it.cloudbreak.newway.context.TestContext;
import com.sequenceiq.it.verification.Call;

import spark.Response;

public class MockVerification implements AssertionV2<StackEntity> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MockVerification.class);

    private final String path;

    private final HttpMethod httpMethod;

    private Integer atLeast;

    private Integer exactTimes;

    private final Collection<Pattern> patternList = new ArrayList<>();

    private final Map<String, Integer> bodyContainsList = new HashMap<>();

    public MockVerification(HttpMethod httpMethod, String path, Integer exactTimes) {
        this.path = path;
        this.httpMethod = httpMethod;
        this.exactTimes = 1;
    }

    public static MockVerification verify(HttpMethod httpMethod, String path) {
        return new MockVerification(httpMethod, path, 1);
    }

    public MockVerification atLeast(int times) {
        atLeast = times;
        return this;
    }

    public MockVerification exactTimes(int times) {
        exactTimes = times;
        return this;
    }

    public MockVerification bodyContains(String text) {
        bodyContainsList.put(text, -1);
        return this;
    }

    public MockVerification bodyContains(String text, int times) {
        bodyContainsList.put(text, times);
        return this;
    }

    public MockVerification bodyRegexp(String regexp) {
        Pattern pattern = Pattern.compile(regexp);
        patternList.add(pattern);
        return this;
    }

    private void logVerify() {
        LOGGER.info("Verification call: " + path);
        LOGGER.info("Body must contains: " + StringUtils.join(bodyContainsList, ","));
        List<String> patternStringList = patternList.stream().map(Pattern::pattern).collect(Collectors.toList());
        LOGGER.info("Body must match: " + StringUtils.join(patternStringList, ","));
    }

    private void checkExactTimes(int times) {
        if (exactTimes != null) {
            if (exactTimes != times) {
                throw new RuntimeException(path + " request should have been invoked exactly " + exactTimes + " times, but it was invoked " + times + " times");
            }
        }
    }

    private void checkAtLeast(int times) {
        if (atLeast != null) {
            if (times < atLeast) {
                throw new RuntimeException(path + " request should have been invoked at least " + atLeast + " times, but it was invoked " + times + " times");
            }
        }
    }

    private int getTimesMatched(Map<Call, Response> requestResponseMap) {
        int times = 0;
        for (Call call : requestResponseMap.keySet()) {
            boolean pathMatched = isPathMatched(call);
            if (call.getMethod().equals(httpMethod.toString()) && pathMatched) {
                int bodyContainsNumber = 0;
                for (Entry<String, Integer> stringIntegerEntry : bodyContainsList.entrySet()) {
                    int count = StringUtils.countMatches(call.getPostBody(), stringIntegerEntry.getKey());
                    int required = stringIntegerEntry.getValue();
                    if ((required < 0 && count > 0) || (count == required)) {
                        bodyContainsNumber++;
                    }
                }
                int patternNumber = 0;
                for (Pattern pattern : patternList) {
                    boolean patternMatch = pattern.matcher(call.getPostBody()).matches();
                    if (patternMatch) {
                        patternNumber++;
                    }
                }
                if (bodyContainsList.size() == bodyContainsNumber && patternList.size() == patternNumber) {
                    times++;
                }
            }
        }
        return times;
    }

    private boolean isPathMatched(Call call) {
        return call.getUri().contains(path);
//        return Pattern.matches(path, call.getUri());
    }

    private void logRequests(Map<Call, Response> requestResponseMap) {
        LOGGER.info("Request received: ");
        requestResponseMap.keySet().forEach(call -> LOGGER.info("Request: " + call));
    }

    @Override
    public StackEntity doAssertion(TestContext testContext, StackEntity entity, CloudbreakClient cloudbreakClient) throws Exception {
        Map<Call, Response> requestResponseMap = testContext.getSparkServer().getRequestResponseMap();
        int matchesCount = getTimesMatched(requestResponseMap);
        logVerify();
//        logRequests(requestResponseMap);
        checkExactTimes(matchesCount);
        checkAtLeast(matchesCount);
        return entity;
    }
}

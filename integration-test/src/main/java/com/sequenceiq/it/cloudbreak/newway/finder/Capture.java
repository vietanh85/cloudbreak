package com.sequenceiq.it.cloudbreak.newway.finder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Capture {

    private static final Logger LOGGER = LoggerFactory.getLogger(Capture.class);

    private Object value;

    public Capture(Object value) {
        this.value = value;
    }

    public void verify(Object newValue) {
        LOGGER.info("verify the old value {} with the new one {}", value, newValue);

        if (!newValue.equals(value)) {
            throw new RuntimeException("Assertion failed, new value:" + newValue + ", oldValue:" + value);
        }
    }
}

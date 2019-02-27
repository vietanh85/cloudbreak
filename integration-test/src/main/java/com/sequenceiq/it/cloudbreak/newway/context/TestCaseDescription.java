package com.sequenceiq.it.cloudbreak.newway.context;

public class TestCaseDescription {

    private final String value;

    private TestCaseDescription(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static TestCaseDescription testCaseDescription(String value) {
        return new TestCaseDescription(value);
    }

    public static class TestCaseDescriptionBuilder {

        private StringBuilder sb;

        public TestCaseDescriptionBuilder() {
            sb = new StringBuilder();
        }

        public TestCaseDescriptionBuilder given(String givenStatement) {
            sb.append("GIVEN " + givenStatement);
            return this;
        }

        public TestCaseDescriptionBuilder when(String whenStatement) {
            sb.append(" WHEN " + whenStatement);
            return this;
        }

        public TestCaseDescription then(String thenStatement) {
            sb.append(" THEN " + thenStatement);
            return testCaseDescription(sb.toString());
        }

    }
}

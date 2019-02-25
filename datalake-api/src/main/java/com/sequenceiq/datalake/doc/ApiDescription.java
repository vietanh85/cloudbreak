package com.sequenceiq.datalake.doc;

public class ApiDescription {

    public static final String JSON = "application/json";

    public static final String REGISTER_DATALAKE_DESCRIPTION = "Operations on Data Lakes";

    public static class DatalakeOpDescription {
        public static final String REGISTER_POST = "register a Data LAke cluster";
    }

    public static class DatalakeNotes {
        public static final String REGISTER_NOTES = "A data lake provides a way for you to centrally apply and enforce authentication, authorization,"
                + " and audit policies across multiple ephemeral workload clusters. ";
    }

    private ApiDescription() {
    }
}

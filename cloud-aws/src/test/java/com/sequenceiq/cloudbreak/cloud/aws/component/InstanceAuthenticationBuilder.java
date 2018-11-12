package com.sequenceiq.cloudbreak.cloud.aws.component;

import com.sequenceiq.cloudbreak.cloud.model.InstanceAuthentication;

public class InstanceAuthenticationBuilder {

    private static final String LOGIN_USER_NAME = "loginusername";

    private static final String PUBLIC_KEY = "pubkey";

    public InstanceAuthentication build(){
        return new InstanceAuthentication(PUBLIC_KEY, "pubkeyid", LOGIN_USER_NAME);
    }
}

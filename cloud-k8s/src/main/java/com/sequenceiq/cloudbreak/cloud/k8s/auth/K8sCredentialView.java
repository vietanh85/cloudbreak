package com.sequenceiq.cloudbreak.cloud.k8s.auth;

import com.google.common.base.Strings;
import com.sequenceiq.cloudbreak.cloud.k8s.K8sConstants;
import com.sequenceiq.cloudbreak.cloud.model.CloudCredential;

public class K8sCredentialView {
    private final CloudCredential cloudCredential;

    public K8sCredentialView(CloudCredential cloudCredential) {
        this.cloudCredential = cloudCredential;
    }

    public String getK8sEndpoint() {
        return cloudCredential.getParameter(K8sConstants.K8S_ENDPOINT_PARAMETER, String.class);
    }

    public String getK8sToken() {
        return cloudCredential.getParameter(K8sConstants.K8S_TOKEN, String.class);
    }

    public Boolean isTokenAuthentication() {
        return !Strings.isNullOrEmpty(cloudCredential.getParameter(K8sConstants.K8S_TOKEN, String.class));
    }
}

package com.sequenceiq.cloudbreak.cloud.model.filesystem;

public class CloudGcsView extends CloudFileSystemView {

    private String serviceAccountEmail;

    private String projectId;

    public CloudGcsView() {
    }

    public String getServiceAccountEmail() {
        return serviceAccountEmail;
    }

    public void setServiceAccountEmail(String serviceAccountEmail) {
        this.serviceAccountEmail = serviceAccountEmail;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }
}

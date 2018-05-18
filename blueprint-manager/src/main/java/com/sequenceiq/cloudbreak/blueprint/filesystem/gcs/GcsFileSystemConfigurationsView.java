package com.sequenceiq.cloudbreak.blueprint.filesystem.gcs;

import java.util.Collection;

import com.sequenceiq.cloudbreak.api.model.filesystem.FileSystemType;
import com.sequenceiq.cloudbreak.api.model.filesystem.GcsFileSystem;
import com.sequenceiq.cloudbreak.blueprint.filesystem.BaseFileSystemConfigurationsView;
import com.sequenceiq.cloudbreak.blueprint.filesystem.StorageLocationView;

public class GcsFileSystemConfigurationsView extends BaseFileSystemConfigurationsView {

    private String defaultBucketName;

    private String projectId;

    private String serviceAccountEmail;

    public GcsFileSystemConfigurationsView(GcsFileSystem gcsFileSystem, Collection<StorageLocationView> locations, boolean deafultFs) {
        super(gcsFileSystem.getStorageContainer(), deafultFs, locations);
        this.defaultBucketName = gcsFileSystem.getDefaultBucketName();
        this.projectId = gcsFileSystem.getProjectId();
        this.serviceAccountEmail = gcsFileSystem.getServiceAccountEmail();
    }

    public String getDefaultBucketName() {
        return defaultBucketName;
    }

    public void setDefaultBucketName(String defaultBucketName) {
        this.defaultBucketName = defaultBucketName;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getServiceAccountEmail() {
        return serviceAccountEmail;
    }

    public void setServiceAccountEmail(String serviceAccountEmail) {
        this.serviceAccountEmail = serviceAccountEmail;
    }

    @Override
    public String getType() {
        return FileSystemType.GCS.name();
    }
}

package com.sequenceiq.cloudbreak.blueprint.filesystem;

import java.util.HashMap;
import java.util.Map;

public class FileSystemConfigQueryObject {

    private final String clusterName;

    private final String storageName;

    private final String blueprintText;

    private FileSystemConfigQueryObject(FileSystemConfigQueryObject.Builder builder) {
        this.storageName = builder.storageName;
        this.clusterName = builder.clusterName;
        this.blueprintText = builder.blueprintText;
    }

    public String getClusterName() {
        return clusterName;
    }

    public String getStorageName() {
        return storageName;
    }

    public String getBlueprintText() {
        return blueprintText;
    }

    public Map<String, Object> asMap() {
        Map<String, Object> templateObject = new HashMap<>();
        templateObject.put("clusterName", getClusterName());
        templateObject.put("storageName", getStorageName());
        templateObject.put("blueprintText", getBlueprintText());
        return templateObject;
    }

    public static class Builder {

        private String clusterName;

        private String storageName;

        private String blueprintText;

        public static Builder builder() {
            return new Builder();
        }

        public Builder withStorageName(String storageName) {
            this.storageName = storageName;
            return this;
        }

        public Builder withClusterName(String clusterName) {
            this.clusterName = clusterName;
            return this;
        }

        public Builder withBlueprintText(String blueprintText) {
            this.blueprintText = blueprintText;
            return this;
        }

        public FileSystemConfigQueryObject build() {
            return new FileSystemConfigQueryObject(this);
        }
    }
}

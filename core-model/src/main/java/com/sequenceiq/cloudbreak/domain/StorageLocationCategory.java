package com.sequenceiq.cloudbreak.domain;

public enum StorageLocationCategory {

    OP_LOGS("opLogs"),
    NOTEBOOK("notebook"),
    WAREHOUSE("warehouse"),
    AUDIT("audit");

    private final String jsonKey;

    StorageLocationCategory(String jsonKey) {
        this.jsonKey = jsonKey;
    }

    public static StorageLocationCategory fromJsonKey(String jsonKey) {
        for (StorageLocationCategory category : StorageLocationCategory.values()) {
            if (category.jsonKey.equals(jsonKey)) {
                return category;
            }
        }
        return null;
    }

    public String getJsonKey() {
        return jsonKey;
    }

}

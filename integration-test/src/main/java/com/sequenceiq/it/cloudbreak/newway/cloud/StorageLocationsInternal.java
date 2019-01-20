package com.sequenceiq.it.cloudbreak.newway.cloud;

import java.util.Map;

class StorageLocationsInternal {

    private Map<String, String> opLogs;

    private Map<String, String> notebook;

    private Map<String, String> warehouse;

    private Map<String, String> audit;

    public Map<String, String> getOpLogs() {
        return opLogs;
    }

    public void setOpLogs(Map<String, String> opLogs) {
        this.opLogs = opLogs;
    }

    public Map<String, String> getNotebook() {
        return notebook;
    }

    public void setNotebook(Map<String, String> notebook) {
        this.notebook = notebook;
    }

    public Map<String, String> getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(Map<String, String> warehouse) {
        this.warehouse = warehouse;
    }

    public Map<String, String> getAudit() {
        return audit;
    }

    public void setAudit(Map<String, String> audit) {
        this.audit = audit;
    }

}

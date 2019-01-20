package com.sequenceiq.cloudbreak.api.model;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sequenceiq.cloudbreak.doc.ModelDescriptions;
import com.sequenceiq.cloudbreak.doc.ModelDescriptions.FileSystem;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel
public class FileSystemResponse extends FileSystemBase {

    @NotNull
    @ApiModelProperty(ModelDescriptions.ID)
    private Long id;

    @ApiModelProperty(FileSystem.OP_LOGS)
    private Map<String, String> opLogs = new HashMap<>();

    @ApiModelProperty(FileSystem.NOTEBOOK)
    private Map<String, String> notebook = new HashMap<>();

    @ApiModelProperty(FileSystem.WAREHOUSE)
    private Map<String, String> warehouse = new HashMap<>();

    @ApiModelProperty(FileSystem.AUDIT)
    private Map<String, String> audit = new HashMap<>();

    @JsonProperty("id")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

package com.sequenceiq.cloudbreak.api.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.sequenceiq.cloudbreak.doc.ModelDescriptions.FileSystem;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("FileSystem")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class FileSystemRequest extends FileSystemBase {

    // TODO add request validation for storage location categories
    @ApiModelProperty(FileSystem.OP_LOGS)
    private Map<String, String> opLogs = new HashMap<>();

    @ApiModelProperty(FileSystem.NOTEBOOK)
    private Map<String, String> notebook = new HashMap<>();

    @ApiModelProperty(FileSystem.WAREHOUSE)
    private Map<String, String> warehouse = new HashMap<>();

    @ApiModelProperty(FileSystem.AUDIT)
    private Map<String, String> audit = new HashMap<>();

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FileSystemRequest)) {
            return false;
        }
        FileSystemRequest that = (FileSystemRequest) o;
        return Objects.equals(getOpLogs(), that.getOpLogs())
                && Objects.equals(getNotebook(), that.getNotebook())
                && Objects.equals(getWarehouse(), that.getWarehouse())
                && Objects.equals(getAudit(), that.getAudit());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOpLogs(), getNotebook(), getWarehouse(), getAudit());
    }

}

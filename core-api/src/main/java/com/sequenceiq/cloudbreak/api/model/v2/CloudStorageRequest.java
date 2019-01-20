package com.sequenceiq.cloudbreak.api.model.v2;

import java.util.HashMap;
import java.util.Map;

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.sequenceiq.cloudbreak.api.model.JsonEntity;
import com.sequenceiq.cloudbreak.api.model.v2.filesystem.AdlsCloudStorageParameters;
import com.sequenceiq.cloudbreak.api.model.v2.filesystem.AdlsGen2CloudStorageParameters;
import com.sequenceiq.cloudbreak.api.model.v2.filesystem.GcsCloudStorageParameters;
import com.sequenceiq.cloudbreak.api.model.v2.filesystem.S3CloudStorageParameters;
import com.sequenceiq.cloudbreak.api.model.v2.filesystem.WasbCloudStorageParameters;
import com.sequenceiq.cloudbreak.doc.ModelDescriptions.ClusterModelDescription;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class CloudStorageRequest implements JsonEntity {

    @Valid
    @ApiModelProperty
    private AdlsCloudStorageParameters adls;

    @Valid
    @ApiModelProperty
    private WasbCloudStorageParameters wasb;

    @Valid
    @ApiModelProperty
    private GcsCloudStorageParameters gcs;

    @Valid
    @ApiModelProperty
    private S3CloudStorageParameters s3;

    @Valid
    @ApiModelProperty
    private AdlsGen2CloudStorageParameters adlsGen2;

    // TODO add request validation for storage location categories
    @ApiModelProperty(ClusterModelDescription.OP_LOGS)
    private Map<String, String> opLogs = new HashMap<>();

    @ApiModelProperty(ClusterModelDescription.NOTEBOOK)
    private Map<String, String> notebook = new HashMap<>();

    @ApiModelProperty(ClusterModelDescription.WAREHOUSE)
    private Map<String, String> warehouse = new HashMap<>();

    @ApiModelProperty(ClusterModelDescription.AUDIT)
    private Map<String, String> audit = new HashMap<>();

    public AdlsCloudStorageParameters getAdls() {
        return adls;
    }

    public void setAdls(AdlsCloudStorageParameters adls) {
        this.adls = adls;
    }

    public WasbCloudStorageParameters getWasb() {
        return wasb;
    }

    public void setWasb(WasbCloudStorageParameters wasb) {
        this.wasb = wasb;
    }

    public GcsCloudStorageParameters getGcs() {
        return gcs;
    }

    public void setGcs(GcsCloudStorageParameters gcs) {
        this.gcs = gcs;
    }

    public S3CloudStorageParameters getS3() {
        return s3;
    }

    public void setS3(S3CloudStorageParameters s3) {
        this.s3 = s3;
    }

    public AdlsGen2CloudStorageParameters getAdlsGen2() {
        return adlsGen2;
    }

    public void setAdlsGen2(AdlsGen2CloudStorageParameters adlsGen2) {
        this.adlsGen2 = adlsGen2;
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

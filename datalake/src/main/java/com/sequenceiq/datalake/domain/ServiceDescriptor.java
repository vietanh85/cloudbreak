package com.sequenceiq.datalake.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;

import com.sequenceiq.cloudbreak.authorization.WorkspaceResource;
import com.sequenceiq.datalake.model.Json;
import com.sequenceiq.datalake.model.JsonToString;
import com.sequenceiq.datalake.model.Secret;
import com.sequenceiq.datalake.model.SecretToString;

@Entity
public class ServiceDescriptor implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "servicedescriptor_generator")
    @SequenceGenerator(name = "servicedescriptor_generator", sequenceName = "servicedescriptor_id_seq", allocationSize = 1)
    private Long id;

    private String workspaceId;

    @ManyToOne
    private DatalakeResources datalakeResources;

    private String serviceName;

    @Convert(converter = JsonToString.class)
    @Column(columnDefinition = "TEXT")
    private Json clusterDefinitionParams;

    @Convert(converter = SecretToString.class)
//    @SecretValue
    private Secret clusterDefinitionSecretParams = Secret.EMPTY;

    @Convert(converter = JsonToString.class)
    @Column(columnDefinition = "TEXT")
    private Json componentsHosts;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public String getName() {
        return "servicedescriptor-" + id;
    }

    public WorkspaceResource getResource() {
        return WorkspaceResource.SERVICE_DESCRIPTOR;
    }

    public DatalakeResources getDatalakeResources() {
        return datalakeResources;
    }

    public void setDatalakeResources(DatalakeResources datalakeResources) {
        this.datalakeResources = datalakeResources;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Json getClusterDefinitionParams() {
        return clusterDefinitionParams;
    }

    public void setClusterDefinitionParam(Json clusterDefinitionParam) {
        this.clusterDefinitionParams = clusterDefinitionParam;
    }

    public Json getClusterDefinitionSecretParams() {
        return new Json(clusterDefinitionSecretParams.getRaw());
    }

    public void setClusterDefinitionSecretParams(Json clusterDefinitionSecretParams) {
        this.clusterDefinitionSecretParams = new Secret(clusterDefinitionSecretParams.getValue());
    }

    public Json getComponentsHosts() {
        return componentsHosts;
    }

    public void setComponentsHosts(Json componentsHosts) {
        this.componentsHosts = componentsHosts;
    }
}

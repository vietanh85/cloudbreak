package com.sequenceiq.datalake.domain;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;

import com.sequenceiq.datalake.model.Json;
import com.sequenceiq.datalake.model.JsonToString;

@Entity
public class DatalakeResources implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "datalakeresources_generator")
    @SequenceGenerator(name = "datalakeresources_generator", sequenceName = "datalakeresources_id_seq", allocationSize = 1)
    private Long id;

    private String workspaceId;

    private Long stackId;

    private String name;

    private String clusterManagerUrl;

    private String clusterManagerIp;

    private String clusterManagerFqdn;

    private String clusterManagerType;

    @Convert(converter = JsonToString.class)
    @Column(columnDefinition = "TEXT", nullable = false)
    private Json datalakeComponents;

    @OneToMany(mappedBy = "datalakeResources", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @MapKey(name = "serviceName")
    private Map<String, ServiceDescriptor> serviceDescriptorMap;

    @ManyToMany(cascade = CascadeType.MERGE)
    private Set<String> rdsConfiguration;

    private String ldapConfiguration;

    private String kerberosConfiguration;

    private String environment;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getWorkspace() {
        return workspaceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public Long getStackId() {
        return stackId;
    }

    public void setStackId(Long stackId) {
        this.stackId = stackId;
    }

    public String getClusterManagerUrl() {
        return clusterManagerUrl;
    }

    public void setClusterManagerUrl(String clusterManagerUrl) {
        this.clusterManagerUrl = clusterManagerUrl;
    }

    public String getClusterManagerIp() {
        return clusterManagerIp;
    }

    public void setClusterManagerIp(String clusterManagerIp) {
        this.clusterManagerIp = clusterManagerIp;
    }

    public String getClusterManagerFqdn() {
        return clusterManagerFqdn;
    }

    public void setClusterManagerFqdn(String clusterManagerFqdn) {
        this.clusterManagerFqdn = clusterManagerFqdn;
    }

    public String getClusterManagerType() {
        return clusterManagerType;
    }

    public void setClusterManagerType(String clusterManagerType) {
        this.clusterManagerType = clusterManagerType;
    }

    public Json getDatalakeComponents() {
        return datalakeComponents;
    }

    public void setDatalakeComponents(Json datalakeComponents) {
        this.datalakeComponents = datalakeComponents;
    }

    public Map<String, ServiceDescriptor> getServiceDescriptorMap() {
        return serviceDescriptorMap;
    }

    public void setServiceDescriptorMap(Map<String, ServiceDescriptor> serviceDescriptorMap) {
        this.serviceDescriptorMap = serviceDescriptorMap;
    }

    public Set<String> getRdsConfiguration() {
        return rdsConfiguration;
    }

    public void setRdsConfiguration(Set<String> rdsConfiguration) {
        this.rdsConfiguration = rdsConfiguration;
    }

    public String getLdapConfiguration() {
        return ldapConfiguration;
    }

    public void setLdapConfiguration(String ldapConfiguration) {
        this.ldapConfiguration = ldapConfiguration;
    }

    public String getKerberosConfiguration() {
        return kerberosConfiguration;
    }

    public void setKerberosConfiguration(String kerberosConfiguration) {
        this.kerberosConfiguration = kerberosConfiguration;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }
}

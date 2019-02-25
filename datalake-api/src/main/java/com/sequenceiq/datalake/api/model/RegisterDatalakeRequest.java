package com.sequenceiq.datalake.api.model;

import java.util.Set;

import javax.validation.constraints.NotEmpty;

import com.sequenceiq.datalake.api.model.ModelDescription.DatalakeModelDescription;

import io.swagger.annotations.ApiModelProperty;

public class RegisterDatalakeRequest {

    @ApiModelProperty(DatalakeModelDescription.DATALAKE_NAME)
    private @NotEmpty String name;

    @ApiModelProperty(DatalakeModelDescription.CLUSTER_MANAGER_URL)
    private @NotEmpty String clusterManagerUrl;

    @ApiModelProperty(DatalakeModelDescription.CLUSTER_MANAGER_TYPE)
    private @NotEmpty String clusterManagerType;

    @ApiModelProperty(DatalakeModelDescription.LDAP_CONFIG_NAME)
    private @NotEmpty String ldapName;

    @ApiModelProperty(DatalakeModelDescription.RDSCONFIG_NAMES)
    private @NotEmpty Set<String> databaseNames;

    @ApiModelProperty(DatalakeModelDescription.KERBEROSCONFIG_NAME)
    private String kerberosName;

    private @NotEmpty String rangerAdminPassword;

    public String getLdapName() {
        return ldapName;
    }

    public void setLdapName(String ldapName) {
        this.ldapName = ldapName;
    }

    public Set<String> getDatabaseNames() {
        return databaseNames;
    }

    public void setDatabaseNames(Set<String> databaseNames) {
        this.databaseNames = databaseNames;
    }

    public String getKerberosName() {
        return kerberosName;
    }

    public void setKerberosName(String kerberosName) {
        this.kerberosName = kerberosName;
    }

    public String getRangerAdminPassword() {
        return rangerAdminPassword;
    }

    public void setRangerAdminPassword(String rangerAdminPassword) {
        this.rangerAdminPassword = rangerAdminPassword;
    }
}

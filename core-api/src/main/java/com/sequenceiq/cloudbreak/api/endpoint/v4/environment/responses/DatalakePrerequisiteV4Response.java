package com.sequenceiq.cloudbreak.api.endpoint.v4.environment.responses;

import java.util.HashSet;
import java.util.Set;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.sequenceiq.cloudbreak.api.endpoint.v4.database.responses.DatabaseV4Response;
import com.sequenceiq.cloudbreak.api.endpoint.v4.kerberos.responses.KerberosV4Response;
import com.sequenceiq.cloudbreak.api.endpoint.v4.ldaps.responses.LdapV4Response;
import com.sequenceiq.cloudbreak.doc.ModelDescriptions;

import io.swagger.annotations.ApiModelProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DatalakePrerequisiteV4Response {

    @NotNull
    @ApiModelProperty(value = ModelDescriptions.LdapConfigModelDescription.RESPONSE)
    private LdapV4Response ldap;

    @ApiModelProperty(value = ModelDescriptions.RDSConfigModelDescription.RESPONSE)
    private Set<DatabaseV4Response> databases = new HashSet<>();

    @NotNull
    @ApiModelProperty(value = ModelDescriptions.KerberosConfigModelDescription.RESPONSE)
    private KerberosV4Response kerberos;

    public LdapV4Response getLdap() {
        return ldap;
    }

    public void setLdap(LdapV4Response ldap) {
        this.ldap = ldap;
    }

    public Set<DatabaseV4Response> getDatabases() {
        return databases;
    }

    public void setDatabases(Set<DatabaseV4Response> databases) {
        this.databases = databases;
    }

    public KerberosV4Response getKerberos() {
        return kerberos;
    }

    public void setKerberos(KerberosV4Response kerberos) {
        this.kerberos = kerberos;
    }
}

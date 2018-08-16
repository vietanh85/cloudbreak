package com.sequenceiq.cloudbreak.controller;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.api.endpoint.v1.CredentialEndpoint;
import com.sequenceiq.cloudbreak.api.model.CredentialRequest;
import com.sequenceiq.cloudbreak.api.model.CredentialResponse;
import com.sequenceiq.cloudbreak.common.type.ResourceEvent;
import com.sequenceiq.cloudbreak.domain.Credential;
import com.sequenceiq.cloudbreak.service.credential.CredentialService;

@Component
@Transactional(TxType.NEVER)
public class CredentialController extends NotificationController implements CredentialEndpoint {

    @Resource
    @Qualifier("conversionService")
    private ConversionService conversionService;

    @Autowired
    private CredentialService credentialService;

    @Override
    public CredentialResponse postPrivate(CredentialRequest credentialRequest) {
        return createCredential(credentialRequest, false);
    }

    @Override
    public CredentialResponse postPublic(CredentialRequest credentialRequest) {
        return createCredential(credentialRequest, true);
    }

    @Override
    public CredentialResponse putPrivate(CredentialRequest credentialRequest) {
        return modifyCredential(credentialRequest, false);
    }

    @Override
    public CredentialResponse putPublic(CredentialRequest credentialRequest) {
        return modifyCredential(credentialRequest, true);
    }

    @Override
    public Set<CredentialResponse> getPrivates() {
        return getPublics();
    }

    @Override
    public Set<CredentialResponse> getPublics() {
        return convertCredentials(credentialService.listForUsersDefaultOrganization());
    }

    @Override
    public CredentialResponse getPrivate(String name) {
        return getPublic(name);
    }

    @Override
    public CredentialResponse getPublic(String name) {
        return convert(credentialService.getByNameForOrganization(name));
    }

    @Override
    public CredentialResponse get(Long id) {
        return convert(credentialService.get(id));
    }

    @Override
    public void delete(Long id) {
        executeAndNotify(user -> credentialService.delete(id), ResourceEvent.CREDENTIAL_DELETED);
    }

    @Override
    public void deletePublic(String name) {
        executeAndNotify(user -> credentialService.delete(name), ResourceEvent.CREDENTIAL_DELETED);
    }

    @Override
    public void deletePrivate(String name) {
        executeAndNotify(user -> credentialService.delete(name), ResourceEvent.CREDENTIAL_DELETED);
    }

    @Override
    public Map<String, String> privateInteractiveLogin(CredentialRequest credentialRequest) {
        return interactiveLogin(credentialRequest, false);
    }

    @Override
    public Map<String, String> publicInteractiveLogin(CredentialRequest credentialRequest) {
        return interactiveLogin(credentialRequest, true);
    }

    private Map<String, String> interactiveLogin(CredentialRequest credentialRequest, boolean publicInAccount) {
        return credentialService.interactiveLogin(convert(credentialRequest, publicInAccount));
    }

    private CredentialResponse createCredential(CredentialRequest credentialRequest, boolean publicInAccount) {
        return convert(credentialService.create(convert(credentialRequest, publicInAccount)));
    }

    private CredentialResponse modifyCredential(CredentialRequest credentialRequest, boolean publicInAccount) {
        return convert(credentialService.update(convert(credentialRequest, publicInAccount)));
    }

    private Credential convert(CredentialRequest json, boolean publicInAccount) {
        Credential converted = conversionService.convert(json, Credential.class);
        converted.setPublicInAccount(publicInAccount);
        return converted;
    }

    private CredentialResponse convert(Credential credential) {
        return conversionService.convert(credential, CredentialResponse.class);
    }

    private Set<CredentialResponse> convertCredentials(Iterable<Credential> credentials) {
        Set<CredentialResponse> jsonSet = new HashSet<>();
        for (Credential credential : credentials) {
            jsonSet.add(convert(credential));
        }
        return jsonSet;
    }
}

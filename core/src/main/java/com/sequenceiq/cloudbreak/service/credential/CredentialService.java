package com.sequenceiq.cloudbreak.service.credential;

import static com.sequenceiq.cloudbreak.api.model.v2.OrganizationStatus.ACTIVE;
import static com.sequenceiq.cloudbreak.util.NameUtil.generateArchiveName;
import static com.sequenceiq.cloudbreak.util.SqlUtil.getProperSqlErrorMessage;

import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.sequenceiq.cloudbreak.controller.exception.NotFoundException;
import com.sequenceiq.cloudbreak.repository.OrganizationResourceRepository;
import com.sequenceiq.cloudbreak.service.AbstractOrganizationAwareResourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.sequenceiq.cloudbreak.api.model.CloudbreakEventsJson;
import com.sequenceiq.cloudbreak.common.model.user.IdentityUser;
import com.sequenceiq.cloudbreak.common.model.user.IdentityUserRole;
import com.sequenceiq.cloudbreak.common.type.APIResourceType;
import com.sequenceiq.cloudbreak.common.type.ResourceEvent;
import com.sequenceiq.cloudbreak.controller.exception.BadRequestException;
import com.sequenceiq.cloudbreak.domain.Credential;
import com.sequenceiq.cloudbreak.domain.organization.Organization;
import com.sequenceiq.cloudbreak.domain.stack.Stack;
import com.sequenceiq.cloudbreak.repository.CredentialRepository;
import com.sequenceiq.cloudbreak.repository.StackRepository;
import com.sequenceiq.cloudbreak.service.AuthorizationService;
import com.sequenceiq.cloudbreak.service.account.AccountPreferencesService;
import com.sequenceiq.cloudbreak.service.messages.CloudbreakMessagesService;
import com.sequenceiq.cloudbreak.service.notification.Notification;
import com.sequenceiq.cloudbreak.service.notification.NotificationSender;
import com.sequenceiq.cloudbreak.service.organization.OrganizationService;
import com.sequenceiq.cloudbreak.service.stack.connector.adapter.ServiceProviderCredentialAdapter;
import com.sequenceiq.cloudbreak.service.user.UserProfileHandler;

@Service
public class CredentialService extends AbstractOrganizationAwareResourceService<Credential> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CredentialService.class);

    @Inject
    private CredentialRepository credentialRepository;

    @Inject
    private StackRepository stackRepository;

    @Inject
    private ServiceProviderCredentialAdapter credentialAdapter;

    @Inject
    private AuthorizationService authorizationService;

    @Inject
    private UserProfileHandler userProfileHandler;

    @Inject
    private AccountPreferencesService accountPreferencesService;

    @Inject
    private NotificationSender notificationSender;

    @Inject
    private CloudbreakMessagesService messagesService;

    @Inject
    private OrganizationService organizationService;

    public Set<Credential> retrievePrivateCredentials() {
        return credentialRepository.findForOrganization(getOrganizationService().getDefaultOrganizationForCurrentUser().getId());
    }

    public Set<Credential> retrieveAccountCredentials(IdentityUser user) {
        Set<String> platforms = accountPreferencesService.enabledPlatforms();
        Organization defaultOrg = getOrganizationService().getDefaultOrganizationForCurrentUser();
        return user.getRoles().contains(IdentityUserRole.ADMIN) && defaultOrg.getStatus() == ACTIVE
                ? credentialRepository.findAllByOrganizationFilterByPlatforms(defaultOrg.getId(), platforms)
                : credentialRepository.findByOrganizationFilterByPlatforms(defaultOrg.getId(), platforms);
    }

    public Credential get(Long id) {
        return credentialRepository.findById(id)
                .orElseThrow(accessDenied(String.format("Access is denied: Credential not found by id '%d'.", id)));
    }

    public Credential getActiveCredentialById(Long id) {
        return Optional.ofNullable(
                credentialRepository.findByIdAndOrganization(id, getOrganizationService().getDefaultOrganizationForCurrentUser().getId()))
                .orElseThrow(accessDenied(String.format("Access is denied: Credential not found by id '%d' in %s organization.",
                        id, getOrganizationService().getDefaultOrganizationForCurrentUser().getName())));
    }

    public Credential getActiveCredentialByName(String name) {
        return Optional.ofNullable(credentialRepository.findOneByName(name, getOrganizationService().getDefaultOrganizationForCurrentUser().getId()))
                .orElseThrow(accessDenied(String.format("Access is denied: Credential not found by name '%s' in %s organization.",
                        name, getOrganizationService().getDefaultOrganizationForCurrentUser().getName())));
    }

    public Map<String, String> interactiveLogin(IdentityUser user, Credential credential) {
        // TODO remove user
        LOGGER.debug("Interactive login: [User: '{}', Account: '{}']", user.getUsername(), user.getAccount());
        credential.setOrganization(organizationService.getDefaultOrganizationForCurrentUser());
        return credentialAdapter.interactiveLogin(credential);
    }

    public Credential create(IdentityUser user, Credential credential) {
        // TODO remove user
        LOGGER.debug("Creating credential: [User: '{}', Account: '{}']", user.getUsername(), user.getAccount());
        credential.setOrganization(organizationService.getDefaultOrganizationForCurrentUser());
        return saveCredentialAndNotify(credential, ResourceEvent.CREDENTIAL_CREATED);
    }

    public Credential modify(IdentityUser user, Credential credential) {
        LOGGER.debug("Modifying credential: [User: '{}', Account: '{}']", user.getUsername(), user.getAccount());
        Credential credentialToModify = credential.isPublicInAccount() ? getPublicCredential(credential.getName(), user)
                : getPrivateCredential(credential.getName(), user);
        if (!credentialToModify.cloudPlatform().equals(credential.cloudPlatform())) {
            throw new BadRequestException("Modifying credential platform is forbidden");
        }
        if (credential.getAttributes() != null) {
            credentialToModify.setAttributes(credential.getAttributes());
        }
        if (credential.getDescription() != null) {
            credentialToModify.setDescription(credential.getDescription());
        }
        if (credential.getTopology() != null) {
            credentialToModify.setTopology(credential.getTopology());
        }
        return saveCredentialAndNotify(credentialToModify, ResourceEvent.CREDENTIAL_MODIFIED);
    }

    @Retryable(value = BadRequestException.class, maxAttempts = 30, backoff = @Backoff(delay = 2000))
    public Credential createWithRetry(Credential credential) {
        return create(credential);
    }

    public Credential create(Credential credential) {
        LOGGER.debug("Creating credential for organization: {}", getOrganizationService().getDefaultOrganizationForCurrentUser().getName());
        Organization organization = organizationService.getDefaultOrganizationForCurrentUser();
        credential.setOrganization(organization);
        return saveCredentialAndNotify(credential, ResourceEvent.CREDENTIAL_CREATED);
    }

    public Credential getPublicCredential(String name, IdentityUser user) {
        return Optional.ofNullable(credentialRepository.findOneByName(name, getOrganizationService().getDefaultOrganizationForCurrentUser().getId()))
                .orElseThrow(accessDenied(String.format("Access is denied: Credential not found by name '%s'", name)));
    }

    public Credential getPrivateCredential(String name, IdentityUser user) {
        return Optional.ofNullable(
                credentialRepository.findByNameAndOrganization(name, getOrganizationService().getDefaultOrganizationForCurrentUser().getId()))
                .orElseThrow(accessDenied(String.format("Access is denied: Credential not found by name '%s'.", name)));
    }

    public void delete(Long id) {
        Credential credential = Optional.ofNullable(
                credentialRepository.findByIdAndOrganization(id, getOrganizationService().getDefaultOrganizationForCurrentUser().getId()))
                .orElseThrow(accessDenied(String.format("Access is denied: Credential not found by id: '%d'.", id)));
        delete(credential);
    }

    public void delete(String name) {
        Credential credential = Optional.ofNullable(
                credentialRepository.findPublicByNameByOrganization(name, getOrganizationService().getDefaultOrganizationForCurrentUser().getId()))
                .orElseThrow(accessDenied(String.format("Access is denied: Credential not found by name '%s'.", name)));
        delete(credential);
    }

    public Credential update(Long id) {
        return credentialAdapter.update(get(id));
    }

    @Override
    public Credential delete(Credential credential) {
        if (canDelete(credential)) {
            userProfileHandler.destroyProfileCredentialPreparation(credential);
            archiveCredential(credential);
        }
        return credential;
    }

    @Override
    protected OrganizationResourceRepository<Credential, Long> repository() {
        return credentialRepository;
    }

    @Override
    protected String resourceName() {
        return "credential";
    }

    @Override
    protected boolean canDelete(Credential credential) {
        if (credential == null) {
            throw new NotFoundException("Credential not found.");
        }
        Set<Stack> stacksForCredential = stackRepository.findByCredential(credential);
        if (!stacksForCredential.isEmpty()) {
            String clusters;
            String message;
            if (stacksForCredential.size() > 1) {
                clusters = stacksForCredential.stream()
                        .map(Stack::getName)
                        .collect(Collectors.joining(", "));
                message = "There are clusters associated with credential config '%s'. Please remove these before deleting the credential. "
                        + "The following clusters are using this credential: [%s]";
            } else {
                clusters = stacksForCredential.iterator().next().getName();
                message = "There is a cluster associated with credential config '%s'. Please remove before deleting the credential. "
                        + "The following cluster is using this credential: [%s]";
            }
            throw new BadRequestException(String.format(message, credential.getName(), clusters));
        }
        return true;
    }

    @Override
    protected void prepareCreation(Credential resource) {

    }

    public void archiveCredential(Credential credential) {
        credential.setName(generateArchiveName(credential.getName()));
        credential.setArchived(true);
        credential.setTopology(null);
        credentialRepository.save(credential);
    }

    private Credential saveCredentialAndNotify(Credential credential, ResourceEvent resourceEvent) {
        credential = credentialAdapter.init(credential);
        Credential savedCredential;
        try {
            savedCredential = credentialRepository.save(credential);
            userProfileHandler.createProfilePreparation(credential);
            sendCredentialNotification(credential, resourceEvent);
        } catch (DataIntegrityViolationException ex) {
            String msg = String.format("Error with resource [%s], %s", APIResourceType.CREDENTIAL, getProperSqlErrorMessage(ex));
            throw new BadRequestException(msg);
        }
        return savedCredential;
    }

    private void sendCredentialNotification(Credential credential, ResourceEvent resourceEvent) {
        CloudbreakEventsJson notification = new CloudbreakEventsJson();
        notification.setEventType(resourceEvent.name());
        notification.setEventTimestamp(new Date().getTime());
        notification.setEventMessage(messagesService.getMessage(resourceEvent.getMessage()));
        notification.setOwner(credential.getOwner());
        notification.setAccount(credential.getAccount());
        notification.setCloud(credential.cloudPlatform());
        notificationSender.send(new Notification<>(notification));
    }

    private Supplier<AccessDeniedException> accessDenied(String accessDeniedMessage) {
        return () -> new AccessDeniedException(accessDeniedMessage);
    }
}

package com.sequenceiq.cloudbreak.service.credential;

import static com.sequenceiq.cloudbreak.util.NameUtil.generateArchiveName;
import static com.sequenceiq.cloudbreak.util.SqlUtil.getProperSqlErrorMessage;

import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.sequenceiq.cloudbreak.authorization.OrganizationResource;
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

    public Set<Credential> listForUsersDefaultOrganization() {
        return credentialRepository.findForOrganization(getDefaultOrg().getId());
    }

    public Credential getByNameForOrganization(String name) {
        return getByNameForOrganization(name, getDefaultOrg().getId());
    }

    @Override
    public Credential create(Credential credential, Long orgId) {
        return super.create(credentialAdapter.init(credential), orgId);
    }

    public Set<Credential> listAvailablesByOrganizationId(Long orgId) {
        return credentialRepository.findForOrganization(orgId);
    }

    public Set<Credential> retrieveAccountCredentials(IdentityUser user) {
        Set<String> platforms = accountPreferencesService.enabledPlatforms();
        return user.getRoles().contains(IdentityUserRole.ADMIN)
                ? credentialRepository.findAllByOrganizationFilterByPlatforms(getDefaultOrg().getId(), platforms)
                : credentialRepository.findByOrganizationFilterByPlatforms(getDefaultOrg().getId(), platforms);
    }

    public Credential get(Long id) {
        return credentialRepository.findById(id)
                .orElseThrow(accessDenied(String.format("Access is denied: Credential not found by id '%d'.", id)));
    }

    public Credential getActiveCredentialById(Long id) {
        return Optional.ofNullable(
                credentialRepository.findByIdAndOrganization(id, getDefaultOrg().getId()))
                .orElseThrow(accessDenied(String.format("Access is denied: Credential not found by id '%d' in %s organization.",
                        id, getDefaultOrg().getName())));
    }

    public Credential getActiveCredentialByName(String name) {
        return Optional.ofNullable(credentialRepository.findOneByName(name, getDefaultOrg().getId()))
                .orElseThrow(accessDenied(String.format("Access is denied: Credential not found by name '%s' in %s organization.",
                        name, getDefaultOrg().getName())));
    }

    public Map<String, String> interactiveLogin(Credential credential) {
        credential.setOrganization(organizationService.getDefaultOrganizationForCurrentUser());
        return credentialAdapter.interactiveLogin(credential);
    }

    public Map<String, String> interactiveLogin(Long organizationId, Credential credential) {
        credential.setOrganization(organizationService.get(organizationId));
        return credentialAdapter.interactiveLogin(credential);
    }

    public Credential create(IdentityUser user, Credential credential) {
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
        LOGGER.debug("Creating credential for organization: {}", getDefaultOrg().getName());
        Organization organization = organizationService.getDefaultOrganizationForCurrentUser();
        credential.setOrganization(organization);
        return create(credential, organization.getId());
    }

    public Credential getPublicCredential(String name, IdentityUser user) {
        return Optional.ofNullable(credentialRepository.findOneByName(name, getDefaultOrg().getId()))
                .orElseThrow(accessDenied(String.format("Unable to access credential with name '%s' in organization: %s", name, getDefaultOrg().getName())));
    }

    public Credential getPrivateCredential(String name, IdentityUser user) {
        return Optional.ofNullable(
                credentialRepository.findByNameAndOrganization(name, getDefaultOrg().getId()))
                .orElseThrow(accessDenied(String.format("Unable to access credential with name '%s' in organization: %s", name, getDefaultOrg().getName())));
    }

    public void delete(Long id) {
        Credential credential = Optional.ofNullable(
                credentialRepository.findByIdAndOrganization(id, getDefaultOrg().getId()))
                .orElseThrow(accessDenied(String.format("Unable to access credential with id '%s' in organization: %s", id, getDefaultOrg().getName())));
        delete(credential);
    }

    public void delete(String name) {
        Credential credential = Optional.ofNullable(
                credentialRepository.findActiveByNameAndOrgId(name, getDefaultOrg().getId()))
                .orElseThrow(accessDenied(String.format("Unable to access credential with name '%s' in organization: %s", name, getDefaultOrg().getName())));
        delete(credential);
    }

    public Credential updateByOrganization(Long organizationId, Credential credential) {
        Credential original = Optional.ofNullable(credentialRepository.findActiveByNameAndOrgId(credential.getName(), organizationId))
                .orElseThrow(accessDenied(String.format("Unable to access credential with name '%s' in organization: %s",
                        credential.getName(), organizationService.get(organizationId).getName())));
        if (original.cloudPlatform() != null && !Objects.equals(credential.cloudPlatform(), original.cloudPlatform())) {
            throw new BadRequestException("Modifying credential platform is forbidden");
        }
        credential.setId(original.getId());
        credential.setOrganization(organizationService.get(organizationId));
        return create(credential, organizationId);
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
    protected OrganizationResource resource() {
        return OrganizationResource.CREDENTIAL;
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
    public Credential getByNameForOrganization(String name, Long organizationId) {
        try {
            return super.getByNameForOrganization(name, organizationId);
        } catch (NotFoundException ignore) {
            throw accessDenied(String.format("Unable to access credential with name '%s' in organization: %s",
                    name, organizationService.get(organizationId))).get();
        }
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

    private Organization getDefaultOrg() {
        return organizationService.getDefaultOrganizationForCurrentUser();
    }
}

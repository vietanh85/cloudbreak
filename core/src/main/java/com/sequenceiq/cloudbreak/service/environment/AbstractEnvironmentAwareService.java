package com.sequenceiq.cloudbreak.service.environment;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.constraints.NotNull;

import org.springframework.core.convert.ConversionService;

import com.sequenceiq.cloudbreak.controller.exception.BadRequestException;
import org.apache.commons.lang3.StringUtils;

import com.sequenceiq.cloudbreak.domain.environment.EnvironmentAwareResource;
import com.sequenceiq.cloudbreak.domain.view.EnvironmentView;
import com.sequenceiq.cloudbreak.repository.environment.EnvironmentResourceRepository;
import com.sequenceiq.cloudbreak.service.AbstractWorkspaceAwareResourceService;

public abstract class AbstractEnvironmentAwareService<T extends EnvironmentAwareResource> extends AbstractWorkspaceAwareResourceService<T> {

    @Inject
    private EnvironmentViewService environmentViewService;

    @Inject
    @Named("conversionService")
    private ConversionService conversionService;

    public T createInEnvironment(T resource, Set<String> environments, @NotNull Long workspaceId) {
        Set<EnvironmentView> environmentViews = environmentViewService.findByNamesInWorkspace(environments, workspaceId);
        validateAttach(environmentViews, environments);
        resource.setEnvironments(environmentViews);
        return createForLoggedInUser(resource, workspaceId);
    }

    public <C> C attachToEnvironmentsAndConvert(String resourceName, Set<String> environments, @NotNull Long workspaceId, Class<C> classToConvert) {
        return conversionService.convert(attachToEnvironments(resourceName, environments, workspaceId), classToConvert);
    }

    public T attachToEnvironments(String resourceName, Set<String> environments, @NotNull Long workspaceId) {
        Set<EnvironmentView> environmentViews = environmentViewService.findByNamesInWorkspace(environments, workspaceId);
        validateAttach(environmentViews, environments);
        T resource = getByNameForWorkspaceId(resourceName, workspaceId);
        resource.getEnvironments().removeAll(environmentViews);
        resource.getEnvironments().addAll(environmentViews);
        return repository().save(resource);
    }

    public Set<T> findByNamesInWorkspace(Set<String> names, @NotNull Long workspaceId) {
        return repository().findAllByNameInAndWorkspaceId(names, workspaceId);
    }

    public Set<T> findAllInWorkspaceAndEnvironment(@NotNull Long workspaceId, String environmentName, Boolean attachGlobalResources) {
        attachGlobalResources = attachGlobalResources == null ? Boolean.TRUE : attachGlobalResources;
        Set<T> resources = new HashSet<>();
        if (!StringUtils.isEmpty(environmentName)) {
            EnvironmentView env = environmentViewService.getByNameForWorkspaceId(environmentName, workspaceId);
            resources.addAll(repository().findAllByWorkspaceIdAndEnvironments(workspaceId, env));
        } else {
            resources.addAll(repository().findAllByWorkspaceIdAndEnvironmentsIsNotNull(workspaceId));
        }
        if (attachGlobalResources) {
            resources.addAll(repository().findAllByWorkspaceIdAndEnvironmentsIsNull(workspaceId));
        }
        return resources;
    }

    protected abstract EnvironmentResourceRepository<T, Long> repository();

    private void validateAttach(Set<EnvironmentView> environmentViews, Set<String> environments) {
        if (environmentViews.size() < environments.size()) {
            Set<String> existingEnvNames = environmentViews.stream().map(EnvironmentView::getName)
                    .collect(Collectors.toSet());
            Set<String> requestedEnvironments = new HashSet<>(environments);
            requestedEnvironments.removeAll(existingEnvNames);
            throw new BadRequestException(String.format("The following environments does not exist in the workspace: [%s], "
                    + "therefore the resource cannot be attached.", requestedEnvironments.stream().collect(Collectors.joining(", "))));
        }
    }

    public EnvironmentViewService environmentViewService() {
        return environmentViewService;
    }
}

package com.sequenceiq.cloudbreak.repository;

import static com.sequenceiq.cloudbreak.authorization.OrganizationPermissions.Action.READ;

import java.util.Collection;
import java.util.Set;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import com.sequenceiq.cloudbreak.aspect.DisableHasPermission;
import com.sequenceiq.cloudbreak.aspect.organization.CheckPermissionsByOrganizationId;
import com.sequenceiq.cloudbreak.aspect.organization.OrganizationResourceType;
import com.sequenceiq.cloudbreak.authorization.OrganizationResource;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sequenceiq.cloudbreak.domain.Credential;
import com.sequenceiq.cloudbreak.domain.Topology;
import com.sequenceiq.cloudbreak.service.EntityType;

@DisableHasPermission
@EntityType(entityClass = Credential.class)
@Transactional(TxType.REQUIRED)
@OrganizationResourceType(resource = OrganizationResource.CREDENTIAL)
public interface CredentialRepository extends OrganizationResourceRepository<Credential, Long> {

    Set<Credential> findAllByCloudPlatform(@Param("cloudPlatform") String cloudPlatform);

    @CheckPermissionsByOrganizationId(action = READ, organizationIdIndex = 1)
    @Query("SELECT b FROM Credential b WHERE b.name= :name AND b.organization.id= :orgId AND b.archived IS FALSE")
    Credential findOneByName(@Param("name") String name, @Param("orgId") Long orgId);

    @CheckPermissionsByOrganizationId(action = READ)
    @Query("SELECT c FROM Credential c WHERE c.organization.id= :orgId AND c.archived IS FALSE")
    Set<Credential> findForOrganization(@Param("orgId") Long orgId);

    @CheckPermissionsByOrganizationId(action = READ)
    @Query("SELECT c FROM Credential c WHERE (c.organization.id= :orgId AND c.publicInAccount= true) AND c.archived IS FALSE "
            + "AND cloudPlatform in (:cloudPlatforms)")
    Set<Credential> findByOrganizationFilterByPlatforms(@Param("orgId") Long orgId, @Param("cloudPlatforms") Collection<String> cloudPlatforms);

    @CheckPermissionsByOrganizationId(action = READ)
    @Query("SELECT c FROM Credential c WHERE c.organization.id= :orgId AND c.archived IS FALSE and cloudPlatform in (:cloudPlatforms)")
    Set<Credential> findAllByOrganizationFilterByPlatforms(@Param("orgId") Long orgId, @Param("cloudPlatforms") Collection<String> cloudPlatforms);

    @CheckPermissionsByOrganizationId(action = READ, organizationIdIndex = 1)
    @Query("SELECT c FROM Credential c WHERE c.name= :name AND c.organization.id= :orgId AND c.archived IS FALSE")
    Credential findActiveByNameAndOrgId(@Param("name") String name, @Param("orgId") Long orgId);

    @CheckPermissionsByOrganizationId(action = READ, organizationIdIndex = 1)
    @Query("SELECT c FROM Credential c WHERE c.id= :id AND c.organization.id= :orgId AND c.archived IS FALSE")
    Credential findByIdAndOrganization(@Param("id") Long id, @Param("orgId") Long orgId);

    @CheckPermissionsByOrganizationId(action = READ, organizationIdIndex = 1)
    @Query("SELECT c FROM Credential c WHERE c.organization.id= :orgId and c.name= :name AND c.archived IS FALSE")
    Credential findByNameAndOrganization(@Param("name") String name, @Param("orgId") Long orgId);

    Set<Credential> findByTopology(Topology topology);
}
package com.sequenceiq.periscope.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PostAuthorize;

import com.sequenceiq.periscope.domain.TimeAlert;

public interface TimeAlertRepository extends CrudRepository<TimeAlert, Long> {

    @PostAuthorize("hasPermission(returnObject,'read')")
    @Override
    Optional<TimeAlert> findById(@Param("id") Long id);

    @PostAuthorize("hasPermission(returnObject,'read')")
    TimeAlert findByCluster(@Param("alertId") Long alertId, @Param("clusterId") Long clusterId);

    List<TimeAlert> findAllByCluster(@Param("clusterId") Long clusterId);
}

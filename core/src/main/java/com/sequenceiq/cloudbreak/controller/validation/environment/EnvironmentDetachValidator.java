package com.sequenceiq.cloudbreak.controller.validation.environment;

import java.util.Set;

import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.api.model.environment.request.EnvironmentDetachRequest;
import com.sequenceiq.cloudbreak.controller.validation.ValidationResult;
import com.sequenceiq.cloudbreak.controller.validation.ValidationResult.ValidationResultBuilder;
import com.sequenceiq.cloudbreak.domain.LdapConfig;
import com.sequenceiq.cloudbreak.domain.ProxyConfig;
import com.sequenceiq.cloudbreak.domain.RDSConfig;

@Component
public class EnvironmentDetachValidator {

    public ValidationResult validate(EnvironmentDetachRequest request,
            Set<LdapConfig> ldapsToAttach, Set<ProxyConfig> proxiesToAttach, Set<RDSConfig> rdssToAttach) {

        ValidationResultBuilder resultBuilder = ValidationResult.builder();
        

        return resultBuilder.build();
    }
}

package com.sequenceiq.datalake.service.ha;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DatalakeNodeConfig {

    @Value("${datalake.instance.node.id:}")
    private String id;

    public String getId() {
        return isNodeIdSpecified() ? id : null;
    }

    public boolean isNodeIdSpecified() {
        return StringUtils.isNoneBlank(id);
    }
}

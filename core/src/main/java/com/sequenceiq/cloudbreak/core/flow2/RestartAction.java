package com.sequenceiq.cloudbreak.core.flow2;

public interface RestartAction {

    void restart(String flowId, long privateId, String flowChainId, String event, Object payload);
}

package com.sequenceiq.cloudbreak.service.flowlog;

import java.util.Map;
import java.util.Queue;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.cedarsoftware.util.io.JsonWriter;
import com.sequenceiq.cloudbreak.cloud.event.Payload;
import com.sequenceiq.cloudbreak.cloud.event.Selectable;
import com.sequenceiq.cloudbreak.core.flow2.FlowState;
import com.sequenceiq.cloudbreak.domain.FlowChainLog;
import com.sequenceiq.cloudbreak.domain.FlowLog;
import com.sequenceiq.cloudbreak.repository.FlowChainLogRepository;
import com.sequenceiq.cloudbreak.repository.FlowLogRepository;
import com.sequenceiq.cloudbreak.service.ha.CloudbreakNodeConfig;

@Service
@Transactional
public class FlowLogService {

    @Inject
    private CloudbreakNodeConfig cloudbreakNodeConfig;

    @Inject
    private FlowLogRepository flowLogRepository;

    @Inject
    private FlowChainLogRepository flowChainLogRepository;

    @Inject
    @Qualifier("JsonWriterOptions")
    private Map<String, Object> writeOptions;

    public FlowLog save(String flowId, long privateId, String flowChanId, String key, Payload payload, Map<Object, Object> variables, Class<?> flowType,
                        FlowState currentState) {
        String payloadJson = JsonWriter.objectToJson(payload, writeOptions);
        String variablesJson = JsonWriter.objectToJson(variables, writeOptions);
        FlowLog flowLog = new FlowLog(payload.getStackId(), flowId, privateId, flowChanId, key, payloadJson, payload.getClass(), variablesJson, flowType,
            currentState.toString());
        flowLog.setCloudbreakNodeId(cloudbreakNodeConfig.getId());
        return flowLogRepository.save(flowLog);
    }

    public void close(Long stackId, String flowId, long privateId) {
        finalize(stackId, flowId, privateId, "FINISHED");
    }

    public void cancel(Long stackId, String flowId, long privateId) {
        finalize(stackId, flowId, privateId, "CANCELLED");
    }

    public FlowLog terminate(Long stackId, String flowId, long privateId) {
        return finalize(stackId, flowId, privateId, "TERMINATED");
    }

    private FlowLog finalize(Long stackId, String flowId, long privateId, String state) {
        flowLogRepository.finalizeByFlowId(flowId);
        FlowLog flowLog = new FlowLog(stackId, flowId, privateId, state, Boolean.TRUE);
        flowLog.setCloudbreakNodeId(cloudbreakNodeConfig.getId());
        return flowLogRepository.save(flowLog);
    }

    public void saveChain(String flowChainId, String parentFlowChainId, Queue<Selectable> chain) {
        String chainJson = JsonWriter.objectToJson(chain);
        FlowChainLog chainLog = new FlowChainLog(flowChainId, parentFlowChainId, chainJson);
        flowChainLogRepository.save(chainLog);
    }
}

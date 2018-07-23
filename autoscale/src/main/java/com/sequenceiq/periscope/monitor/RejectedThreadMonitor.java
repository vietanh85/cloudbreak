package com.sequenceiq.periscope.monitor;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.api.model.AutoscaleStackResponse;
import com.sequenceiq.cloudbreak.util.JsonUtil;
import com.sequenceiq.periscope.domain.Cluster;
import com.sequenceiq.periscope.domain.RejectedThread;
import com.sequenceiq.periscope.monitor.context.ClusterCreationEvaluatorContext;
import com.sequenceiq.periscope.monitor.context.ClusterIdEvaluatorContext;
import com.sequenceiq.periscope.monitor.context.EvaluatorContext;
import com.sequenceiq.periscope.monitor.evaluator.ClusterCreationEvaluator;

@Component
public class RejectedThreadMonitor extends AbstractMonitor<RejectedThread> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RejectedThreadMonitor.class);

    @Override
    public String getIdentifier() {
        return "rejected-thread-monitor";
    }

    @Override
    public String getTriggerExpression() {
        return MonitorUpdateRate.EVERY_TWENTIETH_SEC_RATE_CRON;
    }

    @Override
    public Class<?> getEvaluatorType(RejectedThread rejectedThread) {
        try {
            return Class.forName(rejectedThread.getType());
        } catch (ClassNotFoundException e) {
            LOGGER.info("The given class does not found. " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public EvaluatorContext getContext(RejectedThread rejectedThread) {
        try {
            if (getEvaluatorType(rejectedThread).equals(ClusterCreationEvaluator.class)) {
                AutoscaleStackResponse response = JsonUtil.readValue(rejectedThread.getJson(), AutoscaleStackResponse.class);
                return new ClusterCreationEvaluatorContext(response);
            } else {
                return new ClusterIdEvaluatorContext(JsonUtil.readValue(rejectedThread.getJson(), Cluster.class).getId());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected List<RejectedThread> getMonitored() {
        return getRejectedThreadService().getAllRejectedCluster();
    }

    @Override
    protected RejectedThread save(RejectedThread monitored) {
        return getRejectedThreadService().save(monitored);
    }
}

package com.sequenceiq.periscope.monitor.context;

public class ClusterIdEvaluatorContext implements EvaluatorContext {

    private long clusterId;

    public ClusterIdEvaluatorContext(long clusterId) {
        this.clusterId = clusterId;
    }

    @Override
    public Object getData() {
        return clusterId;
    }
}

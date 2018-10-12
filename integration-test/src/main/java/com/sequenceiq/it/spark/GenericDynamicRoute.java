package com.sequenceiq.it.spark;

import com.sequenceiq.it.cloudbreak.newway.mock.DefaultModel;
import spark.Service;

public abstract class GenericDynamicRoute implements DynamicRoute {

    private final Service service;

    private final DefaultModel model;

    protected GenericDynamicRoute(Service service, DefaultModel model) {
        this.service = service;
        this.model = model;
    }

    public Service getService() {
        return service;
    }

    public DefaultModel getModel() {
        return model;
    }

}
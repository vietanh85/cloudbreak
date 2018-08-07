package com.sequenceiq.it.cloudbreak.newway.mock;

import spark.Service;

public abstract class AbstractModelMock extends AbstractMock {
    private final Model model;

    public AbstractModelMock(Service sparkService, Model model) {
        super(sparkService);
        this.model = model;
    }

    public Model getModel() {
        return model;
    }

}

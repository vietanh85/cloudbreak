package com.sequenceiq.it.spark;

import spark.Route;

public interface IDynamicRoute {

    Route get(String url, Route responseHandler);

    Route put(String url, Route responseHandler);

    Route post(String url, Route responseHandler);

    Route delete(String url, Route responseHandler);

    Route get(String url, StatefulRoute responseHandler);

    Route put(String url, StatefulRoute responseHandler);

    Route post(String url, StatefulRoute responseHandler);

    Route delete(String url, StatefulRoute responseHandler);

    void clearGet(String url);

    void clearPut(String url);

    void clearPost(String url);

    void clearDelete(String url);

}
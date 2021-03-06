package com.sequenceiq.it.cloudbreak.newway.util;

import java.io.IOException;

import javax.ws.rs.BadRequestException;

import com.fasterxml.jackson.databind.JsonNode;
import com.sequenceiq.cloudbreak.util.JsonUtil;

public class ResponseUtil {
    private ResponseUtil() {

    }

    public static String getErrorMessage(Exception ex) {
        if (ex instanceof BadRequestException) {
            try {
                String responseJson = ((BadRequestException) ex).getResponse().readEntity(String.class);
                if (JsonUtil.isValid(responseJson)) {
                    JsonNode jsonNode = JsonUtil.readTree(responseJson);
                    if (jsonNode.has("message")) {
                        return jsonNode.get("message").asText();
                    }
                }
                return responseJson;
            } catch (IOException ignore) {
            }
        }
        return ex.getMessage();
    }
}

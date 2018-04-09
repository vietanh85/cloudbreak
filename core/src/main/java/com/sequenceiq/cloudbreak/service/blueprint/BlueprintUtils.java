package com.sequenceiq.cloudbreak.service.blueprint;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.sequenceiq.cloudbreak.controller.json.JsonHelper;
import com.sequenceiq.cloudbreak.domain.BlueprintParameter;
import com.sequenceiq.cloudbreak.util.JsonUtil;

@Component
public class BlueprintUtils {

    @Inject
    private JsonHelper jsonHelper;

    public int countHostGroups(JsonNode root) {
        int hostGroupCount = 0;
        Iterator<JsonNode> hostGroups = root.get("host_groups").elements();
        while (hostGroups.hasNext()) {
            hostGroups.next();
            hostGroupCount++;
        }
        return hostGroupCount;
    }

    public String getBlueprintName(JsonNode root) {
        return root.get("Blueprints").get("blueprint_name").asText();
    }

    public String getBlueprintHdpVersion(JsonNode root) {
        return root.get("Blueprints").get("stack_version").asText();
    }

    public String getBlueprintStackName(JsonNode root) {
        return root.get("Blueprints").get("stack_name").asText();
    }

    public JsonNode convertStringToJsonNode(String json) {
        return jsonHelper.createJsonFromString(json);
    }

    public List<BlueprintParameter> prepareInputs(JsonNode inputs) throws com.fasterxml.jackson.core.JsonProcessingException {
        Set<BlueprintParameter> blueprintParameters = new HashSet<>();
        if (inputs.isArray()) {
            for (JsonNode objNode : inputs) {
                BlueprintParameter blueprintParameter = JsonUtil.treeToValue(objNode, BlueprintParameter.class);
                blueprintParameters.add(blueprintParameter);
            }
        }
        return new ArrayList<>(blueprintParameters);
    }
}

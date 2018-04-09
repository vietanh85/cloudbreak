package com.sequenceiq.cloudbreak.init.blueprint;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.sequenceiq.cloudbreak.api.model.BlueprintRequest;
import com.sequenceiq.cloudbreak.converter.BlueprintRequestToBlueprintConverter;
import com.sequenceiq.cloudbreak.domain.Blueprint;
import com.sequenceiq.cloudbreak.domain.BlueprintInputParameters;
import com.sequenceiq.cloudbreak.domain.json.Json;
import com.sequenceiq.cloudbreak.service.blueprint.BlueprintUtils;
import com.sequenceiq.cloudbreak.util.FileReaderUtils;

@Service
public class DefaultBlueprintCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultBlueprintCache.class);

    @Value("#{'${cb.blueprint.defaults:}'.split(';')}")
    private List<String> blueprintArray;

    @Inject
    private BlueprintUtils blueprintUtils;

    @Inject
    private BlueprintRequestToBlueprintConverter converter;

    private Map<String, Blueprint> defaultBlueprints = new HashMap<>();

    @PostConstruct
    public void loadBlueprintsFromFile() {
        for (String blueprintStrings : blueprintArray) {
            try {
                BlueprintNames names = new BlueprintNames(blueprintStrings);
                if (names.isBlueprintNamePreConfigured()) {
                    LOGGER.info("Load default blueprint '{}'.", blueprintStrings);
                    BlueprintRequest blueprintJson = new BlueprintRequest();
                    blueprintJson.setName(names.getName());
                    blueprintJson.setDisplayName(names.getDisplayName());
                    JsonNode jsonNode = blueprintUtils.convertStringToJsonNode(FileReaderUtils.readFileFromClasspath(names.getFileName()));
                    blueprintJson.setAmbariBlueprint(jsonNode.get("blueprint").toString());
                    Blueprint bp = converter.convert(blueprintJson);
                    JsonNode inputs = jsonNode.get("inputs");
                    JsonNode description = jsonNode.get("description");
                    bp.setDescription(description == null ? names.getDisplayName() : description.asText(names.getDisplayName()));
                    BlueprintInputParameters inputParameters = new BlueprintInputParameters(blueprintUtils.prepareInputs(inputs));
                    bp.setInputParameters(new Json(inputParameters));
                    defaultBlueprints.put(bp.getName(), bp);
                }
            } catch (IOException e) {
                LOGGER.info("Can not read default blueprint from file: ", e);
            }
        }
    }

    public Map<String, Blueprint> defaultBlueprints() {
        Map<String, Blueprint> result = new HashMap<>();
        defaultBlueprints.entrySet().stream().forEach(e -> result.put(e.getKey(), SerializationUtils.clone(e.getValue())));
        return result;
    }

    public List<String> blueprintArray() {
        return blueprintArray;
    }

    private static class BlueprintNames {
        private String name;

        private String displayName;

        private int length;

        private boolean empty;

        BlueprintNames(String blueprintConfig) {
            if (blueprintConfig.isEmpty()) {
                empty = true;
            } else {
                String[] names = blueprintConfig.split("=");
                length = names.length;
                if (names.length == 1) {
                    name = names[0].trim();
                    displayName = names[0].trim();
                }
                if (names.length == 2) {
                    name = names[1].trim();
                    displayName = names[0].trim();
                }
            }
        }

        public String getFileName() {
            return String.format("defaults/blueprints/%s.bp", name);
        }

        public boolean isBlueprintNamePreConfigured() {
            return !empty && (length == 2 || length == 1) && !name.isEmpty();
        }

        public String getName() {
            return name;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}

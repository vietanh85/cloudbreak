package com.sequenceiq.cloudbreak.blueprint.filesystem.query;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.sequenceiq.cloudbreak.blueprint.BlueprintProcessorFactory;
import com.sequenceiq.cloudbreak.blueprint.BlueprintTextProcessor;
import com.sequenceiq.cloudbreak.blueprint.HandlebarTemplate;
import com.sequenceiq.cloudbreak.blueprint.filesystem.FileSystemConfigQueryObject;
import com.sequenceiq.cloudbreak.blueprint.template.HandlebarUtils;
import com.sequenceiq.cloudbreak.service.CloudbreakResourceReaderService;
import com.sequenceiq.cloudbreak.util.JsonUtil;

@Service
public class FileSystemConfigQueryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileSystemConfigQueryService.class);

    private final Handlebars handlebars = HandlebarUtils.handlebars();

    @Inject
    private CloudbreakResourceReaderService cloudbreakResourceReaderService;

    @Inject
    private BlueprintProcessorFactory blueprintProcessorFactory;

    private ConfigQueryEntries configQueryEntries;

    @PostConstruct
    public void init() {
        String configDefinitions = cloudbreakResourceReaderService.resourceDefinition("cloud-storage-location-specification");
        try {
            configQueryEntries = JsonUtil.readValue(configDefinitions, ConfigQueryEntries.class);
        } catch (IOException e) {
            LOGGER.warn("Cannot initialize configQueryEntries", e);
            configQueryEntries = new ConfigQueryEntries();
        }
    }

    public Set<ConfigQueryEntry> queryParameters(FileSystemConfigQueryObject fileSystemConfigQueryObject) {
        Set<ConfigQueryEntry> filtered = new HashSet<>();

        BlueprintTextProcessor blueprintTextProcessor = blueprintProcessorFactory.get(fileSystemConfigQueryObject.getBlueprintText());
        Map<String, Set<String>> componentsByHostGroup = blueprintTextProcessor.getComponentsByHostGroup();
        componentsByHostGroup.entrySet()
                .forEach(serviceHostgroupEntry -> serviceHostgroupEntry.getValue()
                        .forEach(service -> configQueryEntries.getEntries()
                                .stream()
                                .filter(configQueryEntry -> configQueryEntry.getRelatedService().equalsIgnoreCase(service))
                                .forEach(filtered::add)));

        for (ConfigQueryEntry configQueryEntry : filtered) {
            try {
                configQueryEntry.setDefaultPath(generateConfigWithParameters(fileSystemConfigQueryObject, configQueryEntry.getDefaultPath()));
            } catch (IOException e) {
                configQueryEntry.setDefaultPath(configQueryEntry.getDefaultPath());
            }
        }
        return filtered;
    }

    private String generateConfigWithParameters(FileSystemConfigQueryObject fileSystemConfigQueryObject, String sourceTemplate) throws IOException {
        Template template = handlebars.compileInline(sourceTemplate, HandlebarTemplate.DEFAULT_PREFIX.key(), HandlebarTemplate.DEFAULT_POSTFIX.key());
        Map<String, String> templateObject = new HashMap<>();
        templateObject.put("clusterName", fileSystemConfigQueryObject.getClusterName());
        templateObject.put("storageName", fileSystemConfigQueryObject.getStorageName());
        templateObject.put("blueprintText", fileSystemConfigQueryObject.getBlueprintText());
        return template.apply(templateObject);
    }
}

package com.sequenceiq.it.cloudbreak.newway.action;

import com.sequenceiq.cloudbreak.api.model.FailureReport;
import com.sequenceiq.cloudbreak.api.model.stack.instance.InstanceGroupResponse;
import com.sequenceiq.cloudbreak.api.model.stack.instance.InstanceMetaDataJson;
import com.sequenceiq.it.cloudbreak.newway.CloudbreakClient;
import com.sequenceiq.it.cloudbreak.newway.ProxyCloudbreakClient;
import com.sequenceiq.it.cloudbreak.newway.StackEntity;
import com.sequenceiq.it.cloudbreak.newway.context.TestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.sequenceiq.it.cloudbreak.newway.log.Log.log;
import static com.sequenceiq.it.cloudbreak.newway.log.Log.logJSON;
import static java.lang.String.format;

public class StackNodeUnhealthyAction implements ActionV2<StackEntity> {

    private static final Logger LOGGER = LoggerFactory.getLogger(StackNodeUnhealthyAction.class);

    private final String hostgroup;

    private final int nodeCount;

    public StackNodeUnhealthyAction(String hostgroup, int nodeCount) {
        this.hostgroup = hostgroup;
        this.nodeCount = nodeCount;
    }

    @Override
    public StackEntity action(TestContext testContext, StackEntity entity, CloudbreakClient client) throws Exception {
        log(LOGGER, format(" Name: %s", entity.getRequest().getGeneral().getName()));
        logJSON(LOGGER, format(" Stack delete request:%n"), entity.getRequest());
        ProxyCloudbreakClient client2 = getAutoscaleProxyCloudbreakClient(testContext);
        FailureReport failureReport = new FailureReport();
        failureReport.setFailedNodes(getNodes(getInstanceGroupResponse(entity)));
        try (Response toClose = client2.autoscaleEndpoint().failureReport(Objects.requireNonNull(entity.getResponse().getId()), failureReport)) {
            logJSON(LOGGER, format(" Stack deletion was successful:%n"), entity.getResponse());
            log(LOGGER, format(" ID: %s", entity.getResponse().getId()));
            return entity;
        }
    }

    private ProxyCloudbreakClient getAutoscaleProxyCloudbreakClient(TestContext integrationTestContext) {
        /*return new ProxyCloudbreakClient(
                integrationTestContext.get(CloudbreakTest.CLOUDBREAK_SERVER_ROOT),
                integrationTestContext.get(CloudbreakTest.IDENTITY_URL),
                integrationTestContext.get(CloudbreakTest.AUTOSCALE_SECRET),
                integrationTestContext.get(CloudbreakTest.AUTOSCALE_CLIENTID),
                new ConfigKey(false, true, true));*/
        return null;

    }

    private InstanceGroupResponse getInstanceGroupResponse(StackEntity entity) {
        return entity.getResponse().getInstanceGroups().stream()
                .filter(ig -> ig.getGroup().equals(hostgroup)).collect(Collectors.toList()).get(0);
    }

    private List<String> getNodes(InstanceGroupResponse instanceGroup) {
        return instanceGroup.getMetadata().stream()
                .map(InstanceMetaDataJson::getDiscoveryFQDN).collect(Collectors.toList()).subList(0, nodeCount);
    }

}

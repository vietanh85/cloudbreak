package com.sequenceiq.it.cloudbreak.newway.testcase;

import com.sequenceiq.cloudbreak.api.model.rds.RDSConfigJson;
import com.sequenceiq.cloudbreak.api.model.rds.RdsType;
import com.sequenceiq.cloudbreak.api.model.v2.CloudStorageRequest;
import com.sequenceiq.cloudbreak.api.model.v2.filesystem.AdlsCloudStorageParameters;
import com.sequenceiq.it.cloudbreak.exception.TestFailException;
import com.sequenceiq.it.cloudbreak.newway.Blueprint;
import com.sequenceiq.it.cloudbreak.newway.BlueprintEntity;
import com.sequenceiq.it.cloudbreak.newway.LdapConfig;
import com.sequenceiq.it.cloudbreak.newway.LdapConfigEntity;
import com.sequenceiq.it.cloudbreak.newway.RandomNameCreator;
import com.sequenceiq.it.cloudbreak.newway.RdsConfig;
import com.sequenceiq.it.cloudbreak.newway.RdsConfigEntity;
import com.sequenceiq.it.cloudbreak.newway.Stack;
import com.sequenceiq.it.cloudbreak.newway.StackEntity;
import com.sequenceiq.it.cloudbreak.newway.context.TestContext;
import com.sequenceiq.it.cloudbreak.newway.entity.AmbariEntity;
import com.sequenceiq.it.cloudbreak.newway.entity.ClusterEntity;
import com.sequenceiq.it.cloudbreak.newway.entity.InstanceGroupEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.sequenceiq.it.cloudbreak.newway.cloud.HostGroupType.MASTER;
import static com.sequenceiq.it.cloudbreak.newway.context.RunningParameter.key;

public class SharedServiceTest extends AbstractIntegrationTest {

    private static final String TEST_CONTEXT = "testContext";

    private static final String SHARED_SERVICE_TAG = "shared_services_ready";

    private static final Logger LOGGER = LoggerFactory.getLogger(SharedServiceTest.class);

    private static final String VALID_DL_BP = "{\"Blueprints\":{\"blueprint_name\":\"ownbp\",\"stack_name\":\"HDP\",\"stack_version\":\"2.6\"},\"settings\""
            + ":[{\"recovery_settings\":[]},{\"service_settings\":[]},{\"component_settings\":[]}],\"configurations\":[],\"host_groups\":[{\"name\":\"master\""
            + ",\"configurations\":[],\"components\":[{\"name\":\"METRICS_COLLECTOR\"}],\"cardinality\":\"1\"}]}";

    @Inject
    private RandomNameCreator creator;

    @BeforeMethod
    public void beforeMethod(Object[] data) {
        TestContext testContext = (TestContext) data[0];
        createDefaultUser(testContext);
        createDefaultCredential(testContext);
        createDefaultImageCatalog(testContext);
    }

    @AfterMethod(alwaysRun = true)
    public void tear(Object[] data) {
        ((TestContext) data[0]).cleanupTestContextEntity();
    }

    @Test(dataProvider = TEST_CONTEXT)
    public void testCreateDatalakeCluster(TestContext testContext) {
        String hiveRdsName = creator.getRandomNameForMock();
        String rangerRdsName = creator.getRandomNameForMock();
        String ldapName = creator.getRandomNameForMock();
        String blueprintName = creator.getRandomNameForMock();
        testContext
                .given("hive", RdsConfigEntity.class).valid().withType(RdsType.HIVE.name()).withName(hiveRdsName)
                .when(RdsConfig.postV2())
                .given("ranger", RdsConfigEntity.class).valid().withType(RdsType.RANGER.name()).withName(rangerRdsName)
                .when(RdsConfig.postV2())
                .given(LdapConfigEntity.class).withName(ldapName)
                .when(LdapConfig.postV2())
                .given(BlueprintEntity.class).withName(blueprintName).withTag(List.of(SHARED_SERVICE_TAG), List.of(true)).withAmbariBlueprint(VALID_DL_BP)
                .when(Blueprint.postV2())
                .given("master", InstanceGroupEntity.class).valid().withHostGroup(MASTER).withNodeCount(1)
                .given(StackEntity.class)
                .withInstanceGroups("master")
                .withCluster(datalakeReadyCluster(testContext, hiveRdsName, rangerRdsName, ldapName, blueprintName))
                .capture(entity -> entity.getRequest().getCluster().getRdsConfigNames(), key("rdsConfigNames"))
                .capture(entity -> entity.getRequest().getCluster().getLdapConfigName(), key("ldapConfigName"))
                .when(Stack.postV2())
                .await(STACK_AVAILABLE)
                .verify(entity -> entity.getResponse().getCluster().getRdsConfigs().stream().map(RDSConfigJson::getName).collect(Collectors.toSet()),
                        key("rdsConfigNames"))
                .verify(entity -> entity.getResponse().getCluster().getLdapConfig().getName(), key("ldapConfigName"))
                .then((testContext1, entity, cloudbreakClient) -> checkBlueprintTaggedWithSharedService(entity))
                .validate();
    }

    private CloudStorageRequest cloudStorage() {
        CloudStorageRequest csr = new CloudStorageRequest();
        AdlsCloudStorageParameters adls = new AdlsCloudStorageParameters();
        adls.setAccountName("some");
        adls.setClientId("other");
        adls.setCredential("value");
        adls.setTenantId("here");
        csr.setAdls(adls);
        return csr;
    }

    private ClusterEntity datalakeReadyCluster(TestContext testContext, String hiveRdsName, String rangerRdsName, String ldapName, String blueprintName) {
        return new ClusterEntity(testContext)
                .valid()
                .withRdsConfigNames(Set.of(hiveRdsName, rangerRdsName))
                .withLdapConfigName(ldapName)
                .withCloudStorage(cloudStorage())
                .withAmbariRequest(new AmbariEntity(testContext).valid().withBlueprintName(blueprintName));
    }

    private StackEntity checkBlueprintTaggedWithSharedService(StackEntity stack) {
        Map<String, Object> blueprintTags = stack.getResponse().getCluster().getBlueprint().getTags();
        if (!blueprintTags.containsKey(SHARED_SERVICE_TAG) || blueprintTags.get(SHARED_SERVICE_TAG) == null
                || !(blueprintTags.get(SHARED_SERVICE_TAG) instanceof Boolean)
                || !((Boolean) blueprintTags.get(SHARED_SERVICE_TAG))) {
            throw new TestFailException("shared service tag has not passed properly (or at all) to the blueprint");
        }
        return stack;
    }

}

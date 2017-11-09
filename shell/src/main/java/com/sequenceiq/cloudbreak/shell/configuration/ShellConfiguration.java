package com.sequenceiq.cloudbreak.shell.configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolExecutorFactoryBean;
import org.springframework.shell.CommandLine;
import org.springframework.shell.SimpleShellCommandLineOptions;
import org.springframework.shell.commands.ExitCommands;
import org.springframework.shell.commands.HelpCommands;
import org.springframework.shell.commands.ScriptCommands;
import org.springframework.shell.commands.VersionCommands;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.JLineShellComponent;
import org.springframework.shell.plugin.HistoryFileNameProvider;
import org.springframework.shell.plugin.support.DefaultHistoryFileNameProvider;
import org.springframework.shell.support.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.sequenceiq.cloudbreak.api.model.BlueprintRequest;
import com.sequenceiq.cloudbreak.api.model.BlueprintResponse;
import com.sequenceiq.cloudbreak.api.model.CredentialRequest;
import com.sequenceiq.cloudbreak.api.model.ExecutorType;
import com.sequenceiq.cloudbreak.api.model.InstanceGroupType;
import com.sequenceiq.cloudbreak.api.model.OrchestratorRequest;
import com.sequenceiq.cloudbreak.api.model.RecoveryMode;
import com.sequenceiq.cloudbreak.api.model.SecurityRuleRequest;
import com.sequenceiq.cloudbreak.api.model.StackAuthenticationRequest;
import com.sequenceiq.cloudbreak.api.model.v2.AmbariV2Request;
import com.sequenceiq.cloudbreak.api.model.v2.ClusterV2Request;
import com.sequenceiq.cloudbreak.api.model.v2.InstanceGroupV2Request;
import com.sequenceiq.cloudbreak.api.model.v2.NetworkV2Request;
import com.sequenceiq.cloudbreak.api.model.v2.SecurityGroupV2Request;
import com.sequenceiq.cloudbreak.api.model.v2.StackV2Request;
import com.sequenceiq.cloudbreak.api.model.v2.TemplateV2Request;
import com.sequenceiq.cloudbreak.client.CloudbreakClient;
import com.sequenceiq.cloudbreak.client.CloudbreakClient.CloudbreakClientBuilder;
import com.sequenceiq.cloudbreak.client.ConfigKey;
import com.sequenceiq.cloudbreak.client.SSLConnectionException;
import com.sequenceiq.cloudbreak.shell.transformer.ResponseTransformer;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Spring bean definitions.
 */
@Configuration
public class ShellConfiguration {

    private static final String CLIENT_ID = "cloudbreak_shell";

    private static final String IDENTITY_ROOT_CONTEXT = "/identity";

    @Value("${cloudbreak.address:}")
    private String cloudbreakAddress;

    @Value("${identity.address:}")
    private String identityServerAddress;

    @Value("${sequenceiq.user:}")
    private String user;

    @Value("${sequenceiq.password:}")
    private String password;

    @Value("${rest.debug:false}")
    private boolean restDebug;

    @Value("${cert.validation:true}")
    private boolean certificateValidation;

    @Value("${cmdfile:}")
    private String cmdFile;

    @Value("${server.contextPath:/cb}")
    private String cbRootContextPath;

    @Bean
    static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @SuppressFBWarnings("DM_EXIT")
    @Bean
    public CloudbreakClient cloudbreakClient() {
        try {

            CloudbreakClient cloudbreakClient = new CloudbreakClientBuilder("https://dps-dev.eng.hortonworks.com/cb", "https://dps-dev.eng.hortonworks.com/identity", "cloudbreak_shell")
                .withCredential("khorvath@hortonworks.com", "password") // Hortonworks OKTA credentials
                .withCertificateValidation(false)
                .withIgnorePreValidation(true)
                .build();

            // Create an Ambari blueprint (either from URL or raw String)
            BlueprintRequest blueprintRequest = new BlueprintRequest();
            blueprintRequest.setName("EDW-ETL: Apache Hive 1.2.1, Apache Spark 2.1 -krisz");
            blueprintRequest.setAmbariBlueprint("{\"Blueprints\":{\"blueprint_name\":\"hdp26-etl-edw-spark2\",\"stack_name\":\"HDP\",\"stack_version\":\"2.6\"},\"settings\":[{\"recovery_settings\":[]},{\"service_settings\":[{\"name\":\"HIVE\",\"credential_store_enabled\":\"false\"}]},{\"component_settings\":[]}],\"configurations\":[{\"core-site\":{\"fs.trash.interval\":\"4320\"}},{\"hdfs-site\":{\"dfs.namenode.safemode.threshold-pct\":\"0.99\"}},{\"hive-site\":{\"hive.exec.compress.output\":\"true\",\"hive.merge.mapfiles\":\"true\",\"hive.server2.tez.initialize.default.sessions\":\"true\",\"hive.server2.transport.mode\":\"http\"}},{\"mapred-site\":{\"mapreduce.job.reduce.slowstart.completedmaps\":\"0.7\",\"mapreduce.map.output.compress\":\"true\",\"mapreduce.output.fileoutputformat.compress\":\"true\"}},{\"yarn-site\":{\"yarn.acl.enable\":\"true\"}}],\"host_groups\":[{\"name\":\"master\",\"configurations\":[],\"components\":[{\"name\":\"APP_TIMELINE_SERVER\"},{\"name\":\"HCAT\"},{\"name\":\"HDFS_CLIENT\"},{\"name\":\"HISTORYSERVER\"},{\"name\":\"HIVE_CLIENT\"},{\"name\":\"HIVE_METASTORE\"},{\"name\":\"HIVE_SERVER\"},{\"name\":\"JOURNALNODE\"},{\"name\":\"MAPREDUCE2_CLIENT\"},{\"name\":\"METRICS_COLLECTOR\"},{\"name\":\"METRICS_MONITOR\"},{\"name\":\"MYSQL_SERVER\"},{\"name\":\"NAMENODE\"},{\"name\":\"PIG\"},{\"name\":\"RESOURCEMANAGER\"},{\"name\":\"SECONDARY_NAMENODE\"},{\"name\":\"SPARK2_CLIENT\"},{\"name\":\"SPARK2_JOBHISTORYSERVER\"},{\"name\":\"TEZ_CLIENT\"},{\"name\":\"WEBHCAT_SERVER\"},{\"name\":\"YARN_CLIENT\"},{\"name\":\"ZOOKEEPER_CLIENT\"},{\"name\":\"ZOOKEEPER_SERVER\"}],\"cardinality\":\"1\"},{\"name\":\"worker\",\"configurations\":[],\"components\":[{\"name\":\"HIVE_CLIENT\"},{\"name\":\"TEZ_CLIENT\"},{\"name\":\"SPARK2_CLIENT\"},{\"name\":\"DATANODE\"},{\"name\":\"METRICS_MONITOR\"},{\"name\":\"NODEMANAGER\"}],\"cardinality\":\"1+\"},{\"name\":\"compute\",\"configurations\":[],\"components\":[{\"name\":\"HIVE_CLIENT\"},{\"name\":\"TEZ_CLIENT\"},{\"name\":\"SPARK2_CLIENT\"},{\"name\":\"METRICS_MONITOR\"},{\"name\":\"NODEMANAGER\"}],\"cardinality\":\"1+\"}]}");
            // Send the request
            cloudbreakClient.blueprintEndpoint().postPrivate(blueprintRequest);

            // Create a credential to use Azure (the credential can be re-used later by referencing its name)
            CredentialRequest credentialRequest = new CredentialRequest();
            credentialRequest.setCloudPlatform("AZURE");
            credentialRequest.setName("azurecredential");
            Map<String, Object> credentialParameters = new HashMap<>();
            credentialParameters.put("subscriptionId", "value");
            credentialParameters.put("tenantId", "value");
            credentialParameters.put("secretKey", "value");
            credentialParameters.put("accessKey", "value");
            credentialRequest.setParameters(credentialParameters);

            // Send the request
            cloudbreakClient.credentialEndpoint().postPrivate(credentialRequest);

            StackV2Request stackRequest = new StackV2Request();
            stackRequest.setName("dataplane-demo-cluster-1");
            stackRequest.setRegion("West US 2");

            // Salt is the only option for public clouds
            OrchestratorRequest orchestrator = new OrchestratorRequest();
            orchestrator.setType("SALT");
            stackRequest.setOrchestrator(orchestrator);

            // Configure the instance groups (must match the host groups in the blueprint)
            // master (NN, RM) and Ambari server
            InstanceGroupV2Request master = new InstanceGroupV2Request();
            master.setNodeCount(1);
            master.setGroup("master");
            master.setType(InstanceGroupType.GATEWAY); // Ambari server instance
            TemplateV2Request masterTemplate = new TemplateV2Request();
            masterTemplate.setVolumeCount(2);
            masterTemplate.setVolumeSize(100);
            masterTemplate.setVolumeType("Standard_LRS");
            masterTemplate.setInstanceType("Standard_D3_v2");
            Map<String, Object> masterProperties = new HashMap<>();
            masterProperties.put("sshLocation", "0.0.0.0/0");
            masterProperties.put("encrypted", "false");
            masterTemplate.setParameters(masterProperties);
            master.setTemplate(masterTemplate);
            // Configure access for the master instance
            SecurityGroupV2Request masterSecurityGroup = new SecurityGroupV2Request();
            // Cloudbreak must be able to SSH to the Ambari server instance
            SecurityRuleRequest sshRule = new SecurityRuleRequest();
            sshRule.setSubnet("0.0.0.0/0");
            sshRule.setPorts("22");
            sshRule.setProtocol("tcp");
            sshRule.setModifiable(false);
            // HTTPS access for the users
            SecurityRuleRequest httpsRule = new SecurityRuleRequest();
            httpsRule.setSubnet("0.0.0.0/0");
            httpsRule.setPorts("443");
            httpsRule.setProtocol("tcp");
            httpsRule.setModifiable(false);
            // Cloudbreak must be able to access the Ambari server instance on port 9443 (2-way-ssl connection)
            SecurityRuleRequest saltApiRule = new SecurityRuleRequest();
            saltApiRule.setSubnet("0.0.0.0/0");
            saltApiRule.setPorts("9443");
            saltApiRule.setProtocol("tcp");
            saltApiRule.setModifiable(false);
            masterSecurityGroup.setSecurityRules(Arrays.asList(sshRule, httpsRule, saltApiRule));
            master.setSecurityGroup(masterSecurityGroup);
            master.setRecoveryMode(RecoveryMode.MANUAL); // Fault tolerance support in CB is disabled for now

            // worker host group
            InstanceGroupV2Request worker = new InstanceGroupV2Request();
            worker.setNodeCount(1);
            worker.setGroup("worker");
            worker.setType(InstanceGroupType.CORE); // Only 1 instance group can have 'GATEWAY' role (which will be the Ambari server)
            TemplateV2Request workerTemplate = new TemplateV2Request();
            workerTemplate.setVolumeCount(2);
            workerTemplate.setVolumeSize(100);
            workerTemplate.setVolumeType("Standard_LRS");
            workerTemplate.setInstanceType("Standard_D3_v2");
            Map<String, Object> workerProperties = new HashMap<>();
            workerProperties.put("sshLocation", "0.0.0.0/0");
            workerProperties.put("encrypted", "false");
            workerTemplate.setParameters(workerProperties);
            worker.setTemplate(workerTemplate);
            // Configure access for the worker instance
            SecurityGroupV2Request workerSecurityGroup = new SecurityGroupV2Request();
            // SSH access for the users, Cloudbreak does not need SSH access the any nodes other than the Ambari server node
            workerSecurityGroup.setSecurityRules(Collections.singletonList(sshRule));
            worker.setSecurityGroup(workerSecurityGroup);
            worker.setRecoveryMode(RecoveryMode.MANUAL); // Fault tolerance support in CB is disabled for now

            // compute host group with 0 instances (later can be scaled up)
            InstanceGroupV2Request compute = new InstanceGroupV2Request();
            compute.setNodeCount(0);
            compute.setGroup("compute");
            compute.setType(InstanceGroupType.CORE); // Only 1 instance group can have 'GATEWAY' role (which will be the Ambari server)
            TemplateV2Request computeTemplate = new TemplateV2Request();
            computeTemplate.setVolumeCount(2);
            computeTemplate.setVolumeSize(100);
            computeTemplate.setVolumeType("Standard_LRS");
            computeTemplate.setInstanceType("Standard_D3_v2");
            Map<String, Object> computeProperties = new HashMap<>();
            computeProperties.put("sshLocation", "0.0.0.0/0");
            computeProperties.put("encrypted", "false");
            computeTemplate.setParameters(computeProperties);
            compute.setTemplate(computeTemplate);
            // Configure access for the compute instance
            SecurityGroupV2Request computeSecurityGroup = new SecurityGroupV2Request();
            // SSH access for the users, Cloudbreak does not need SSH access the any nodes other than the Ambari server node
            computeSecurityGroup.setSecurityRules(Collections.singletonList(sshRule));
            compute.setSecurityGroup(computeSecurityGroup);
            compute.setRecoveryMode(RecoveryMode.MANUAL); // Fault tolerance support in CB is disabled for now

            // Set the instance groups (must match the Ambari host groups)
            stackRequest.setInstanceGroups(Arrays.asList(master, worker, compute));

            // Configure SSH key for the instances
            StackAuthenticationRequest authentication = new StackAuthenticationRequest();
            authentication.setPublicKey("ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDZiOMYPWpvLAVBlL8ElDgmCtXxvT0P4tNGg6nzUqYCM9Ie0UJjr0JDVwwlwYL/Y3yau42vaxnx16ZOpfaQoAQbafL5uqAZaASqgH02IJamIQ4HrQRv88awAmd6HewFrm4VXKMsVXAzBX+vc+MwknXSg7ucetUQTtCQrhrSWDmBKCk/4PndWM7KjcnftbZ4+Br2Vsr5OB3js4bDWu5I2zh2NV8IfVDobiK2JyqdoRmJ2r8HpUuCQwwWAlUpOA5YRMZ/FPQRoKFq5SL55A3ubDUDvqPiHR4hwbrSpl1D4yOTaXhDid/wQkLpPbHknoBmAnTwJnBqiyVlsMCdOS9N3woH");
            stackRequest.setStackAuthentication(authentication);

            // Configure the VPC for the cluster (create a new VPC with subnet CIDR)
            NetworkV2Request network = new NetworkV2Request();
            network.setSubnetCIDR("10.0.0.0/16");
            stackRequest.setNetwork(network);

            // Use the previously created credential
            stackRequest.setCredentialName("azurecredential");

            // Use the Azure provided hostname and domain name (custom can be used as well)
            stackRequest.setClusterNameAsSubdomain(false);
            stackRequest.setHostgroupNameAsHostname(false);

            // Configure the Ambari cluster on top of the created stack on Azure
            ClusterV2Request clusterRequest = new ClusterV2Request();
            clusterRequest.setExecutorType(ExecutorType.DEFAULT);
            AmbariV2Request ambari = new AmbariV2Request();
            ambari.setBlueprintName("EDW-ETL: Apache Hive 1.2.1, Apache Spark 2.1 -krisz"); // The host groups must match the instance groups
            ambari.setUserName("admin");
            ambari.setPassword("admin");
            clusterRequest.setAmbariRequest(ambari);
            stackRequest.setClusterRequest(clusterRequest);

            // Send the request
            cloudbreakClient.stackV2Endpoint().postPrivate(stackRequest);

            return cloudbreakClient;
        } catch (Exception e) {
            System.out.println(String.format("%s Try to start the shell with --cert.validation=false parameter.", e.getMessage()));
            System.exit(1);
        }
        return null;
    }

    @Bean
    ResponseTransformer responseTransformer() {
        return new ResponseTransformer();
    }

    @Bean
    HistoryFileNameProvider defaultHistoryFileNameProvider() {
        return new DefaultHistoryFileNameProvider();
    }

    @Bean(name = "shell")
    JLineShellComponent shell() {
        return new JLineShellComponent();
    }

    @Bean
    CommandLine commandLine() throws Exception {
        String[] args = !cmdFile.isEmpty() ? new String[]{"--cmdfile", cmdFile} : new String[0];
        return SimpleShellCommandLineOptions.parseCommandLine(args);
    }

    @Bean
    ThreadPoolExecutorFactoryBean getThreadPoolExecutorFactoryBean() {
        return new ThreadPoolExecutorFactoryBean();
    }

    @Bean
    ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    CommandMarker exitCommand() {
        return new ExitCommands();
    }

    @Bean
    CommandMarker versionCommands() {
        return new VersionCommands();
    }

    @Bean
    CommandMarker helpCommands() {
        return new HelpCommands();
    }

    @Bean
    CommandMarker scriptCommands() {
        return new ScriptCommands();
    }

}

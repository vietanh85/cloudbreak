package com.sequenceiq.cloudbreak.cloud.gcp.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.sequenceiq.cloudbreak.cloud.model.CloudCredential;
import com.sequenceiq.cloudbreak.cloud.model.Network;

public class GcpStackUtilTest {

    @Test
    public void projectIdConverterWithNewNameRestrictions() {
        String projectId = GcpStackUtil.getProjectId(cloudCredential("siq-haas"));
        Assert.assertEquals("siq-haas", projectId);
        projectId = GcpStackUtil.getProjectId(cloudCredential("siq-haas123"));
        Assert.assertEquals("siq-haas123", projectId);
        projectId = GcpStackUtil.getProjectId(cloudCredential("Siq-haas123"));
        Assert.assertEquals("siq-haas123", projectId);
    }

    @Test
    public void projectIdConverterWithOldNameRestrictions() {
        String projectId = GcpStackUtil.getProjectId(cloudCredential("echo:siq-haas"));
        Assert.assertEquals("echo-siq-haas", projectId);
        projectId = GcpStackUtil.getProjectId(cloudCredential("echo:>siq>-haas"));
        Assert.assertEquals("echo--siq--haas", projectId);
        projectId = GcpStackUtil.getProjectId(cloudCredential("e?cho:siq-haas123"));
        Assert.assertEquals("e-cho-siq-haas123", projectId);
        projectId = GcpStackUtil.getProjectId(cloudCredential("echo:siq-hasfdsf12?as"));
        Assert.assertEquals("echo-siq-hasfdsf12-as", projectId);
    }

    private CloudCredential cloudCredential(String projectId) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("projectId", projectId);
        return new CloudCredential(1L, "test", parameters);
    }

    @Test
    public void testNewSubnetInExistingNetworkNoNetwork() {
        Network network = new Network(new HashMap<>());
        assertFalse(GcpStackUtil.isNewSubnetInExistingNetwork(network));
    }

    @Test
    public void testNewSubnetInExistingNetworkWithNetwork() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(GcpStackUtil.NETWORK_ID, "asdf");
        Network network = new Network(parameters);
        assertTrue(GcpStackUtil.isNewSubnetInExistingNetwork(network));
    }

    @Test
    public void testNewSubnetInExistingNetworkWithNetworkWithSubnet() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(GcpStackUtil.NETWORK_ID, "asdf");
        parameters.put(GcpStackUtil.SUBNET_ID, "asdf");
        Network network = new Network(parameters);
        assertFalse(GcpStackUtil.isNewSubnetInExistingNetwork(network));
    }

}
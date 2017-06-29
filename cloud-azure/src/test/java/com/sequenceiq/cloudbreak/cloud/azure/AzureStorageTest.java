package com.sequenceiq.cloudbreak.cloud.azure;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AzureStorageTest {

    @Mock
    private AzureUtils azureUtils;

    @InjectMocks
    private AzureStorage underTest = new AzureStorage();

    @Test
    public void encryptionShouldBeFalseIfParametersDoesNotContainsField() {
        Map<String, String> map = new HashMap<>();
        Boolean encryptStorageAccount = underTest.encryptStorageAccount(map);
        Assert.assertFalse(encryptStorageAccount);
    }

    @Test
    public void encryptionShouldBeTrueIfParametersContainsTrueField() {
        Map<String, String> map = new HashMap<>();
        map.put("encryptStorage", "true");
        Boolean encryptStorageAccount = underTest.encryptStorageAccount(map);
        Assert.assertTrue(encryptStorageAccount);
    }

    @Test
    public void keyVaultRequiredShouldBeTrueIfParametersContainsField() {
        Map<String, String> map = new HashMap<>();
        Boolean keyVaultRequired = underTest.keyVaultRequired(map);
        Assert.assertFalse(keyVaultRequired);
    }

    @Test
    public void keyVaultRequiredShouldBeFalseIfParametersDoesNotContainField() {
        Map<String, String> map = new HashMap<>();
        map.put("keyVaultUrl", "https://keyvaulttest.vault.azure.net/keys/testkey/12323423423423");
        Boolean keyVaultRequired = underTest.keyVaultRequired(map);
        Assert.assertTrue(keyVaultRequired);
    }

    @Test
    public void keyVaultParametersShouldBeRepresentedIfParametersContainsUrl() {
        Map<String, String> map = new HashMap<>();
        map.put("keyVaultUrl", "https://keyvaulttest.vault.azure.net/keys/testkey/12323423423423");
        String keyVaultName = underTest.keyVaultName(map);
        String keyVersion = underTest.keyVersion(map);
        String keyName = underTest.keyName(map);

        Assert.assertEquals(keyVaultName, "keyvaulttest");
        Assert.assertEquals(keyVersion, "12323423423423");
        Assert.assertEquals(keyName, "testkey");
    }

}
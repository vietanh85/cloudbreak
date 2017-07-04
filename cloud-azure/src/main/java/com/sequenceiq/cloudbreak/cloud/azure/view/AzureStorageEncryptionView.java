package com.sequenceiq.cloudbreak.cloud.azure.view;

import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Strings;

public class AzureStorageEncryptionView {

    public static final int KEYNAME_POSITION = 2;

    public static final int KEYVERSION_POSITION = 3;

    private Map<String, Object> parameters = new HashMap<>();
    private Boolean encryptStorageAccount = Boolean.FALSE;
    private Boolean keyVaultRequired = Boolean.FALSE;
    private String keyVaultName;
    private String keyVaultUrl;
    private String keyName;
    private String keyVersion;

    public AzureStorageEncryptionView(Map<String, Object> parameters) {
        this.parameters = parameters;
        this.encryptStorageAccount = encryptStorageAccount(parameters);
        this.keyVaultUrl = keyVaultUrl(parameters);
        this.keyVaultRequired = keyVaultRequired(parameters);
        this.keyVaultName = keyVaultName(parameters);
        this.keyName = keyName(parameters);
        this.keyVersion = keyVersion(parameters);
    }

    public Boolean keyVaultRequired(Map<String, Object> parameters) {
        if (Strings.isNullOrEmpty(parameters.get("keyVaultUrl").toString())) {
            return false;
        }
        return true;
    }

    public String keyVaultUrl(Map<String, Object> parameters) {
        String keyVaultUrl = parameters.get("keyVaultUrl").toString();
        if (Strings.isNullOrEmpty(keyVaultUrl)) {
            return "";
        } else {
            return keyVaultUrl;
        }
    }

    public String keyVaultName(Map<String, Object> parameters) {
        String keyVaultUrl = keyVaultUrl(parameters);
        if (Strings.isNullOrEmpty(keyVaultUrl)) {
            return "";
        } else {
            return getKeyVaultUrlWithoutProtocol(keyVaultUrl).split("/")[0].split("\\.")[0];
        }
    }

    public String keyName(Map<String, Object> parameters) {
        String keyVaultUrl = keyVaultUrl(parameters);
        if (Strings.isNullOrEmpty(keyVaultUrl)) {
            return "";
        } else {
            return getKeyVaultUrlWithoutProtocol(keyVaultUrl).split("/")[KEYNAME_POSITION];
        }
    }

    public String keyVersion(Map<String, Object> parameters) {
        String keyVaultUrl = keyVaultUrl(parameters);
        if (Strings.isNullOrEmpty(keyVaultUrl)) {
            return "";
        } else {
            return getKeyVaultUrlWithoutProtocol(keyVaultUrl).split("/")[KEYVERSION_POSITION];
        }
    }

    private String getKeyVaultUrlWithoutProtocol(String url) {
        return url.replaceAll("http://", "").replaceAll("https://", "");
    }

    public Boolean encryptStorageAccount(Map<String, Object> parameters) {
        String encryptStorage = parameters.get("encryptStorage").toString();
        if (Strings.isNullOrEmpty(encryptStorage)) {
            return false;
        }
        return Boolean.valueOf(encryptStorage);
    }

    public Boolean getEncryptStorageAccount() {
        return encryptStorageAccount;
    }

    public Boolean getKeyVaultRequired() {
        return keyVaultRequired;
    }

    public String getKeyVaultName() {
        return keyVaultName;
    }

    public String getKeyVaultUrl() {
        return keyVaultUrl;
    }

    public String getKeyName() {
        return keyName;
    }

    public String getKeyVersion() {
        return keyVersion;
    }
}

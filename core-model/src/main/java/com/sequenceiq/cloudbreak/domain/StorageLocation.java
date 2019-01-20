package com.sequenceiq.cloudbreak.domain;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class StorageLocation implements ProvisionEntity {

    private String id;

    private StorageLocationCategory category;

    private String configFile;

    private String property;

    private String value;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public StorageLocationCategory getCategory() {
        return category;
    }

    public void setCategory(StorageLocationCategory category) {
        this.category = category;
    }

    public String getConfigFile() {
        return configFile;
    }

    public void setConfigFile(String configFile) {
        this.configFile = configFile;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof StorageLocation)) {
            return false;
        }
        StorageLocation that = (StorageLocation) o;
        return Objects.equals(getId(), that.getId())
                && Objects.equals(getCategory(), that.getCategory())
                && Objects.equals(getConfigFile(), that.getConfigFile())
                && Objects.equals(getProperty(), that.getProperty())
                && Objects.equals(getValue(), that.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getCategory(), getConfigFile(), getProperty(), getValue());
    }

}

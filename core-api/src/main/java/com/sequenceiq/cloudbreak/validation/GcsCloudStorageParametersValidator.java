package com.sequenceiq.cloudbreak.validation;

import com.sequenceiq.cloudbreak.api.model.v2.filesystem.GcsCloudStorageParameters;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class GcsCloudStorageParametersValidator implements ConstraintValidator<ValidGcsCloudStorageParameters, GcsCloudStorageParameters> {

    private String failMessage = "";

    @Override
    public void initialize(ValidGcsCloudStorageParameters constraintAnnotation) {
    }

    @Override
    public boolean isValid(GcsCloudStorageParameters value, ConstraintValidatorContext context) {
        boolean result;
        if (!isDefaultBucketNameValid(value.getDefaultBucketName())
                || !isProjectIdValid(value.getProjectId())
                || !isServiceAccountEmailValid(value.getServiceAccountEmail())) {
            ValidatorUtil.addConstraintViolation(context, failMessage, "status");
            result = false;
        } else {
            result = true;
        }
        return result;
    }

    private boolean isDefaultBucketNameValid(String defaultBucketName) {
        return isNotNull(defaultBucketName, "defaultBucketName should not be null!");
    }

    private boolean isProjectIdValid(String projectId) {
        return isNotNull(projectId, "defaultBucketName should not be null!");
    }

    private boolean isServiceAccountEmailValid(String serviceAccountEmail) {
        return isNotNull(serviceAccountEmail, "serviceAccountEmail should not be null!");
    }

    private boolean isNotNull(String content, String messageIfFails) {
        if (content == null) {
            failMessage = messageIfFails;
            return false;
        } else {
            return true;
        }
    }

}

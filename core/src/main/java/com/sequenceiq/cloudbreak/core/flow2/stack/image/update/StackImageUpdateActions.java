package com.sequenceiq.cloudbreak.core.flow2.stack.image.update;

import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Service;

import com.sequenceiq.cloudbreak.api.model.Status;
import com.sequenceiq.cloudbreak.cloud.event.model.EventStatus;
import com.sequenceiq.cloudbreak.core.flow2.event.StackImageUpdateTriggerEvent;
import com.sequenceiq.cloudbreak.core.flow2.stack.FlowMessageService;
import com.sequenceiq.cloudbreak.core.flow2.stack.Msg;
import com.sequenceiq.cloudbreak.core.flow2.stack.StackContext;
import com.sequenceiq.cloudbreak.reactor.api.event.StackEvent;
import com.sequenceiq.cloudbreak.reactor.api.event.StackFailureEvent;
import com.sequenceiq.cloudbreak.reactor.api.event.stack.ImageUpdateEvent;
import com.sequenceiq.cloudbreak.service.image.StatedImage;
import com.sequenceiq.cloudbreak.service.stack.connector.OperationException;

@Service
public class StackImageUpdateActions {

    private static final Logger LOGGER = LoggerFactory.getLogger(StackImageUpdateActions.class);

    @Inject
    private StackImageUpdateService stackImageUpdateService;

    @Inject
    private FlowMessageService flowMessageService;

    @Bean(name = "CHECK_IMAGE_VERSIONS_STATE")
    public Action<?, ?> checkImageVersion() {
        return new AbstractStackImageUpdateAction<>(StackImageUpdateTriggerEvent.class) {
            @Override
            protected void doExecute(StackContext context, StackImageUpdateTriggerEvent payload, Map<Object, Object> variables) {
                flowMessageService.fireEventAndLog(context.getStack().getId(), Msg.STACK_IMAGE_UPDATE_STARTED, Status.UPDATE_IN_PROGRESS.name());
                if (!stackImageUpdateService.isCbVersionOk(context.getStack())) {
                    throw new OperationException("Stack must be created at least with Cloudbreak version [" + StackImageUpdateService.MIN_VERSION + "]");
                }
                StatedImage newImage = stackImageUpdateService.getNewImageIfVersionsMatch(context.getStack(), payload.getNewImageId(),
                        payload.getImageCatalogName(), payload.getImageCatalogUrl());
                sendEvent(context.getFlowId(), new ImageUpdateEvent(StackImageUpdateEvent.CHECK_IMAGE_VERESIONS_FINISHED_EVENT.event(),
                        context.getStack().getId(), newImage));
            }
        };
    }

    @Bean(name = "CHECK_PACKAGE_VERSIONS_STATE")
    public Action<?, ?> checkPackageVersions() {
        return new AbstractStackImageUpdateAction<>(ImageUpdateEvent.class) {
            @Override
            protected void doExecute(StackContext context, ImageUpdateEvent payload, Map<Object, Object> variables) {
                CheckResult checkResult = stackImageUpdateService.checkPackageVersions(context.getStack(), payload.getImage());
                if (checkResult.getStatus() == EventStatus.FAILED) {
                    throw new OperationException(checkResult.getMessage());
                }
                sendEvent(context.getFlowId(), new ImageUpdateEvent(StackImageUpdateEvent.CHECK_PACKAGE_VERSIONS_FINISHED_EVENT.event(),
                        context.getStack().getId(), payload.getImage()));
            }
        };
    }

    @Bean(name = "UPDATE_IMAGE_STATE")
    public Action<?, ?> updateImage() {
        return new AbstractStackImageUpdateAction<>(ImageUpdateEvent.class) {
            @Override
            protected void doExecute(StackContext context, ImageUpdateEvent payload, Map<Object, Object> variables) {
                stackImageUpdateService.storeNewImageComponent(context.getStack(), payload.getImage());
                flowMessageService.fireEventAndLog(context.getStack().getId(), Msg.STACK_IMAGE_UPDATE_FINISHED, Status.AVAILABLE.name());
                sendEvent(context.getFlowId(), new StackEvent(StackImageUpdateEvent.UPDATE_IMAGE_FINESHED_EVENT.event(), context.getStack().getId()));
            }
        };
    }

    @Bean(name = "STACK_IMAGE_UPDATE_FAILED_STATE")
    public Action<?, ?> handleImageUpdateFailure() {
        return new AbstractStackImageUpdateAction<>(StackFailureEvent.class) {
            @Override
            protected void doExecute(StackContext context, StackFailureEvent payload, Map<Object, Object> variables) {
                LOGGER.error("Error during Stack image update flow:", payload.getException());
                flowMessageService.fireEventAndLog(context.getStack().getId(), Msg.STACK_IMAGE_UPDATE_FAILED, Status.UPDATE_FAILED.name(),
                        payload.getException().getMessage());
                sendEvent(context.getFlowId(), new StackEvent(StackImageUpdateEvent.STACK_IMAGE_UPDATE_FAILE_HANDLED_EVENT.event(), context.getStack().getId()));
            }
        };
    }
}

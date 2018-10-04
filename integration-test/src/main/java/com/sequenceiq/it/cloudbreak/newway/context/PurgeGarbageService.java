package com.sequenceiq.it.cloudbreak.newway.context;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sequenceiq.it.cloudbreak.newway.ApplicationContextProvider;
import com.sequenceiq.it.cloudbreak.newway.CredentialEntity;
import com.sequenceiq.it.cloudbreak.newway.ImageCatalogEntity;
import com.sequenceiq.it.cloudbreak.newway.StackEntity;

@Service
public class PurgeGarbageService {

    public <T> void purge() {
        TestContext testContext = ApplicationContextProvider.getBean(TestContext.class);
        testContext.as();
        purge(testContext, purgables(testContext));
        testContext.shutdown();
    }

    private <T> void purge(TestContext testContext, List<Purgable<T>> purgables) {
        testContext.when(StackEntity.class, (testContext1, entity, cloudbreakClient) -> {
                    purgables.forEach(purgable ->
                            purgable.getAll(cloudbreakClient).stream()
                                    .filter(purgable::deletable)
                                    .forEach(e -> purgable.delete(e, cloudbreakClient)));
                    return entity;
                });
    }

    private <T, P extends Purgable<T>> List<P> purgables(TestContext testContext) {
        return List.of((P) testContext.init(StackEntity.class), (P) testContext.init(CredentialEntity.class), (P) testContext.init(ImageCatalogEntity.class));
    }
}

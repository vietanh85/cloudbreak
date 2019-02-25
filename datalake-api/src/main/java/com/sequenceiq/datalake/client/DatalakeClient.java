package com.sequenceiq.datalake.client;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import org.glassfish.jersey.client.proxy.WebResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sequenceiq.cloudbreak.client.CaasClient;
import com.sequenceiq.cloudbreak.client.ConfigKey;
import com.sequenceiq.cloudbreak.client.RestClientUtil;
import com.sequenceiq.cloudbreak.client.TokenRequest;
import com.sequenceiq.cloudbreak.client.TokenUnavailableException;
import com.sequenceiq.datalake.api.DatalakeApi;
import com.sequenceiq.datalake.api.endpoint.v1.DatalakeEndpoint;

import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;

public class DatalakeClient {

    private static final Form EMPTY_FORM = new Form();

    private static final String TOKEN_KEY = "TOKEN";

    private final Logger logger = LoggerFactory.getLogger(DatalakeClient.class);

    private final ExpiringMap<String, String> tokenCache;

    private final Client client;

    private final String datalakeAddress;

    private String refreshToken;

    private final CaasClient caasClient;

    private WebTarget webTarget;

    private EndpointHolder endpointHolder;

    private DatalakeClient(String datalakeAddress, String caasProtocol, String caasAddress, String refreshToken, ConfigKey configKey) {
        client = RestClientUtil.get(configKey);
        this.datalakeAddress = datalakeAddress;
        this.refreshToken = refreshToken;
        caasClient = new CaasClient(caasProtocol, caasAddress, configKey);
        tokenCache = configTokenCache();
        logger.info("AutoscaleClient has been created with token. datalake: {}, token: {}, configKey: {}", datalakeAddress, refreshToken, configKey);
    }

    public DatalakeEndpoint datalakeEndpoint() {
        return refreshIfNeededAndGet(DatalakeEndpoint.class);
    }

    private ExpiringMap<String, String> configTokenCache() {
        return ExpiringMap.builder().variableExpiration().expirationPolicy(ExpirationPolicy.CREATED).build();
    }

    private synchronized <T> T refreshIfNeededAndGet(Class<T> clazz) {
        if (refreshToken != null) {
            String accessToken = tokenCache.get(TOKEN_KEY);
            if (accessToken == null || endpointHolder == null) {
                TokenRequest tokenRequest = new TokenRequest();
                tokenRequest.setRefreshToken(refreshToken);
                accessToken = caasClient.getAccessToken(tokenRequest);
                tokenCache.put(TOKEN_KEY, accessToken, ExpirationPolicy.CREATED, 1, TimeUnit.MINUTES);
                MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
                headers.add("Authorization", "Bearer " + accessToken);
                webTarget = client.target(datalakeAddress).path(DatalakeApi.API_ROOT_CONTEXT);
                endpointHolder = new EndpointHolder(newEndpoint(DatalakeEndpoint.class, headers));
                logger.info("Endpoints have been renewed for AutoscaleClient");
            }
            return (T) endpointHolder.endpoints.stream().filter(e -> e.getClass().equals(clazz)).findFirst().get();
        }
        throw new TokenUnavailableException("No Refresh token provided for AutoscaleClient!");
    }

    private <C> C newEndpoint(Class<C> resourceInterface, MultivaluedMap<String, Object> headers) {
        return WebResourceFactory.newResource(resourceInterface, webTarget, false, headers, Collections.emptyList(), EMPTY_FORM);
    }

    private static class EndpointHolder {
        private final List<?> endpoints;

        EndpointHolder(Object... endpoints) {
            this.endpoints = Arrays.asList(endpoints);
        }
    }

    public static class AutoscaleClientBuilder {

        private final String datalakeAddress;

        private String refreshToken;

        private String caasProtocol;

        private String caasAddress;

        private boolean debug;

        private boolean secure = true;

        private boolean ignorePreValidation;

        public AutoscaleClientBuilder(String datalakeAddress, String caasProtocol, String caasAddress) {
            this.datalakeAddress = datalakeAddress;
            this.caasProtocol = caasProtocol;
            this.caasAddress = caasAddress;
        }

        public AutoscaleClientBuilder withCredential(String refreshToken) {
            this.refreshToken = refreshToken;
            return this;
        }

        public AutoscaleClientBuilder withDebug(boolean debug) {
            this.debug = debug;
            return this;
        }

        public AutoscaleClientBuilder withCertificateValidation(boolean secure) {
            this.secure = secure;
            return this;
        }

        public AutoscaleClientBuilder withIgnorePreValidation(boolean ignorePreValidation) {
            this.ignorePreValidation = ignorePreValidation;
            return this;
        }

        public DatalakeClient build() {
            ConfigKey configKey = new ConfigKey(secure, debug, ignorePreValidation);
            return new DatalakeClient(datalakeAddress, caasProtocol, caasAddress, refreshToken, configKey);
        }
    }
}

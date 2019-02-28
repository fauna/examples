package com.faunadb.persistence.common;

import com.faunadb.client.FaunaClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.net.MalformedURLException;

@Configuration
public class FaunaClientConfig {

    @Autowired
    private FaunaClientProperties faunaProperties;

    /**
     * It initiates a singleton {@link FaunaClient} instance
     * using the settings defined at {@link FaunaClientProperties}.
     * This allows the {@link FaunaClient} to be properly injected
     * in any other application component.
     *
     * @return a singleton {@link FaunaClient} instance
     * @throws MalformedURLException if the provided endpoint is an invalid URL
     */
    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
    public FaunaClient faunaClient() throws MalformedURLException {
        FaunaClient client =
            FaunaClient.builder()
                .withEndpoint(faunaProperties.getEndpoint())
                .withSecret(faunaProperties.getSecret())
                .build();

        return client;
    }

}

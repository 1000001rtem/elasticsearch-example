package ru.eremin.elasticsearch.example.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.reactive.ReactiveElasticsearchClient;
import org.springframework.data.elasticsearch.client.reactive.ReactiveRestClients;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchConverter;
import org.springframework.data.elasticsearch.core.convert.MappingElasticsearchConverter;
import org.springframework.data.elasticsearch.core.mapping.SimpleElasticsearchMappingContext;
import org.springframework.data.elasticsearch.repository.config.EnableReactiveElasticsearchRepositories;
import org.springframework.web.reactive.function.client.ExchangeStrategies;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "elasticsearch")
@EnableReactiveElasticsearchRepositories
public class ElasticsearchConfiguration {

    private List<String> urls = new ArrayList<>();

    @Bean
    public ReactiveElasticsearchClient reactiveElasticsearchClient() {
        ClientConfiguration clientConfiguration = ClientConfiguration.builder()
                .connectedTo(urls.toArray(String[]::new))
                .withWebClientConfigurer(webClient -> {
                    final ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
                            .codecs(configurer -> configurer.defaultCodecs()
                                    .maxInMemorySize(-1))
                            .build();
                    return webClient.mutate().exchangeStrategies(exchangeStrategies).build();
                })
                .build();
        return ReactiveRestClients.create(clientConfiguration);
    }

    @Bean
    public SimpleElasticsearchMappingContext simpleElasticsearchMappingContext() {
        return new SimpleElasticsearchMappingContext();
    }

    @Bean
    public ElasticsearchConverter elasticsearchConverter(SimpleElasticsearchMappingContext simpleElasticsearchMappingContext) {
        return new MappingElasticsearchConverter(simpleElasticsearchMappingContext);
    }

    @Bean
    public ReactiveElasticsearchTemplate reactiveElasticsearchTemplate(
            ReactiveElasticsearchClient reactiveElasticsearchClient,
            ElasticsearchConverter elasticsearchConverter
    ) {
        return new ReactiveElasticsearchTemplate(reactiveElasticsearchClient, elasticsearchConverter);
    }
}

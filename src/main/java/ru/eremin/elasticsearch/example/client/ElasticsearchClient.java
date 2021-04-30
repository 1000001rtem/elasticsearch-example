package ru.eremin.elasticsearch.example.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.eremin.elasticsearch.example.client.config.WebClientConfiguration;
import ru.eremin.elasticsearch.example.client.dto.CreateIndexRequest;
import ru.eremin.elasticsearch.example.client.dto.CreateIndexResponse;

@Component
public class ElasticsearchClient {

    private final WebClient client;

    public ElasticsearchClient(@Qualifier(WebClientConfiguration.ELASTIC_CLIENT) WebClient client) {
        this.client = client;
    }

    public Mono<CreateIndexResponse> createIndex(CreateIndexRequest request) {
        return client.put()
                .uri("/" + request.getIndexName())
                .header("Content-Type", "application/json")
                .header("Accept", "*/*")
                .bodyValue(request.getSettings())
                .retrieve()
                .bodyToMono(CreateIndexResponse.class);
    }

    public Mono<Boolean> indexExist(String indexName) {
        return client.head()
                .uri("/" + indexName)
                .header("Content-Type", "application/json")
                .header("Accept", "*/*")
                .exchangeToMono(response -> Mono.just(response.statusCode().is2xxSuccessful()));
    }
}

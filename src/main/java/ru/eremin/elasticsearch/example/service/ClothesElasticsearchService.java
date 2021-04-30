package ru.eremin.elasticsearch.example.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.eremin.elasticsearch.example.client.ElasticsearchClient;
import ru.eremin.elasticsearch.example.client.dto.CreateIndexRequest;
import ru.eremin.elasticsearch.example.client.dto.CreateIndexResponse;
import ru.eremin.elasticsearch.example.util.Utils;

@Service
public class ClothesElasticsearchService implements ElasticsearchService {

    private final ElasticsearchClient client;

    public ClothesElasticsearchService(ElasticsearchClient client) {
        this.client = client;
    }

    @Override
    public Mono<Boolean> indexExist() {
        return client.indexExist("clothes");
    }

    @Override
    public Mono<CreateIndexResponse> createIndex() {
        return client.indexExist("clothes")
                .flatMap(resp -> {
                    if (resp == Boolean.FALSE) {
                        return getIndexSettings()
                                .map(settings -> new CreateIndexRequest("clothes", settings))
                                .flatMap(client::createIndex);
                    } else {
                        return Mono.empty();
                    }
                });
    }

    private Mono<String> getIndexSettings() {
        return Mono.fromCallable(() -> Utils.readResource("elasticsearch/clothesIndexSettings.json"));
    }
}

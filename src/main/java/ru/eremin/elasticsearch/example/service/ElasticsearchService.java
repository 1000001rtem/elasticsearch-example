package ru.eremin.elasticsearch.example.service;

import reactor.core.publisher.Mono;
import ru.eremin.elasticsearch.example.client.dto.CreateIndexResponse;

public interface ElasticsearchService {

    Mono<Boolean> indexExist();

    Mono<CreateIndexResponse> createIndex();
}

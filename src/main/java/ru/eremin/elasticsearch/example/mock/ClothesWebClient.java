package ru.eremin.elasticsearch.example.mock;

import reactor.core.publisher.Flux;
import ru.eremin.elasticsearch.example.storage.model.Clothes;

public interface ClothesWebClient {
    Flux<Clothes> findAll();
}

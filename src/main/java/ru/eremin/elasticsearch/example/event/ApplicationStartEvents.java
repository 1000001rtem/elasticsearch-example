package ru.eremin.elasticsearch.example.event;

import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import ru.eremin.elasticsearch.example.mock.ClothesWebClient;
import ru.eremin.elasticsearch.example.service.ElasticsearchService;
import ru.eremin.elasticsearch.example.storage.model.Clothes;
import ru.eremin.elasticsearch.example.storage.repository.ClothesRepository;

@Component
public class ApplicationStartEvents {

    private final ElasticsearchService elasticsearchService;
    private final ClothesRepository repository;
    private final ClothesWebClient clothesWebClient;

    public ApplicationStartEvents(
            ElasticsearchService elasticsearchService,
            ClothesRepository repository,
            ClothesWebClient clothesWebClient
    ) {
        this.elasticsearchService = elasticsearchService;
        this.repository = repository;
        this.clothesWebClient = clothesWebClient;
    }

    @EventListener(ApplicationStartedEvent.class)
    public void createClothesIndexIfNotExist() {
        elasticsearchService.createIndex()
                .thenMany(fillStorage())
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
    }

    private Flux<Clothes> fillStorage() {
        return clothesWebClient.findAll()
                .flatMap(repository::save);
    }
}

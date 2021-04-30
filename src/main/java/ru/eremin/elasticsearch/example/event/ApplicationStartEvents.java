package ru.eremin.elasticsearch.example.event;

import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import ru.eremin.elasticsearch.example.service.ElasticsearchService;
import ru.eremin.elasticsearch.example.storage.model.Clothes;
import ru.eremin.elasticsearch.example.storage.repository.ClothesRepository;

import java.util.List;

@Component
public class ApplicationStartEvents {

    private final ElasticsearchService elasticsearchService;
    private final ClothesRepository repository;

    public ApplicationStartEvents(ElasticsearchService elasticsearchService, ClothesRepository repository) {
        this.elasticsearchService = elasticsearchService;
        this.repository = repository;
    }

    @EventListener(ApplicationStartedEvent.class)
    public void createClothesIndexIfNotExist() {
        elasticsearchService.createIndex()
                .thenMany(init())
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
    }

    private Flux<Clothes> init() {
        return repository.saveAll(List.of(
                new Clothes()
                        .setGender("Male")
                        .setCategory("Jacket")
                        .setSeason("Spring")
                        .setBrand("BenSherman")
                        .setColor("Black")
                        .setActive(true),
                new Clothes()
                        .setGender("Male")
                        .setCategory("Jacket")
                        .setSeason("Spring")
                        .setBrand("Stone Island")
                        .setColor("Green")
                        .setActive(true),
                new Clothes()
                        .setGender("Male")
                        .setCategory("Jeans")
                        .setSeason("Summer")
                        .setBrand("Levi")
                        .setColor("Blue")
                        .setActive(true),
                new Clothes()
                        .setGender("Male")
                        .setCategory("Short")
                        .setSeason("Spring")
                        .setBrand("Benny")
                        .setColor("Red")
                        .setActive(true),
                new Clothes()
                        .setGender("Male")
                        .setCategory("Smoking")
                        .setSeason("Winter")
                        .setBrand("Armani")
                        .setColor("Black")
                        .setActive(true),
                new Clothes()
                        .setGender("Female")
                        .setCategory("Jacket")
                        .setSeason("Spring")
                        .setBrand("BenSherman")
                        .setColor("Black")
                        .setActive(true),
                new Clothes()
                        .setGender("Female")
                        .setCategory("Jacket")
                        .setSeason("Spring")
                        .setBrand("Stone Island")
                        .setColor("Green")
                        .setActive(true),
                new Clothes()
                        .setGender("Female")
                        .setCategory("Jeans")
                        .setSeason("Summer")
                        .setBrand("Levi")
                        .setColor("Blue")
                        .setActive(true),
                new Clothes()
                        .setGender("Female")
                        .setCategory("Short")
                        .setSeason("Spring")
                        .setBrand("Benny")
                        .setColor("Red")
                        .setActive(true),
                new Clothes()
                        .setGender("Female")
                        .setCategory("Dress")
                        .setSeason("Summer")
                        .setBrand("Armani")
                        .setColor("Yellow")
                        .setActive(true)
        ));
    }
}

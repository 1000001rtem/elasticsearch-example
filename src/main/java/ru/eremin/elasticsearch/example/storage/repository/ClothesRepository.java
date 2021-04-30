package ru.eremin.elasticsearch.example.storage.repository;

import org.springframework.data.elasticsearch.core.ReactiveElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.eremin.elasticsearch.example.storage.model.Clothes;
import ru.eremin.elasticsearch.example.util.ClothesQueryCreator;

import java.util.List;

@Component
public class ClothesRepository {
    private final ReactiveElasticsearchTemplate template;

    public ClothesRepository(ReactiveElasticsearchTemplate template) {
        this.template = template;
    }

    public Flux<Clothes> findAll() {
        return template.search(ClothesQueryCreator.EMPTY_QUERY, Clothes.class)
                .map(SearchHit::getContent);
    }

    public Flux<Clothes> findByQuery(NativeSearchQuery query) {
        return template.search(query, Clothes.class)
                .map(SearchHit::getContent);
    }

    public Mono<Clothes> findById(String id) {
        return template.get(id, Clothes.class);
    }

    public Mono<Clothes> save(Clothes clothes) {
        return template.save(clothes);
    }

    public Flux<Clothes> saveAll(List<Clothes> clothes) {
        return template.saveAll(clothes, Clothes.class);
    }

    public Mono<String> delete(String id) {
        return template.delete(id, Clothes.class);
    }
}

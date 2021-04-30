package ru.eremin.elasticsearch.example.service;

import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import ru.eremin.elasticsearch.example.controller.dto.Condition;
import ru.eremin.elasticsearch.example.storage.model.Clothes;
import ru.eremin.elasticsearch.example.storage.repository.ClothesRepository;
import ru.eremin.elasticsearch.example.util.ClothesQueryCreator;

import java.util.List;

@Service
public class ClothesService {

    private final ClothesRepository repository;

    public ClothesService(ClothesRepository repository) {
        this.repository = repository;
    }

    public Flux<Clothes> findAll() {
        return repository.findAll();
    }

    public Flux<Clothes> search(List<Condition> conditions) {
        final NativeSearchQuery query = new ClothesQueryCreator()
                .addConditions(conditions)
                .createQuery();

        return repository.findByQuery(query);
    }
}

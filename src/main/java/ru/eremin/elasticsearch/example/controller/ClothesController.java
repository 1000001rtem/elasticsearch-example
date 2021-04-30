package ru.eremin.elasticsearch.example.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import ru.eremin.elasticsearch.example.controller.dto.ClothesSearchRequest;
import ru.eremin.elasticsearch.example.service.ClothesService;
import ru.eremin.elasticsearch.example.storage.model.Clothes;

@RestController
@RequestMapping("/api/clothes")
public class ClothesController {

    private static final Logger logger = LoggerFactory.getLogger(ClothesController.class);

    private final ClothesService service;

    public ClothesController(ClothesService service) {
        this.service = service;
    }

    @PostMapping(value = "search", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<Clothes> search(@RequestBody ClothesSearchRequest request) {
        return service.search(request.getConditions());
    }

    @GetMapping("all")
    public Flux<Clothes> all() {
        return service.findAll();
    }
}

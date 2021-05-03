package ru.eremin.elasticsearch.example.mock;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import ru.eremin.elasticsearch.example.storage.model.Clothes;

import java.util.List;

@Component
public class ClothesMockClient implements ClothesWebClient {
    @Override
    public Flux<Clothes> findAll() {
        return Flux.fromIterable(createTestClothes());
    }

    private List<Clothes> createTestClothes() {
        return List.of(
                new Clothes()
                        .setGender("Male")
                        .setCategory("Jacket")
                        .setSeason("Spring")
                        .setBrand("Бен Шерман")
                        .setColor("Чёрный")
                        .setActive(true),
                new Clothes()
                        .setGender("Male")
                        .setCategory("Jacket")
                        .setSeason("Spring")
                        .setBrand("Стон Исланд")
                        .setColor("Зелёный")
                        .setActive(true),
                new Clothes()
                        .setGender("Male")
                        .setCategory("Jeans")
                        .setSeason("Summer")
                        .setBrand("Левайсы")
                        .setColor("Голубой")
                        .setActive(true),
                new Clothes()
                        .setGender("Male")
                        .setCategory("Short")
                        .setSeason("Spring")
                        .setBrand("Бенни")
                        .setColor("Красный")
                        .setActive(true),
                new Clothes()
                        .setGender("Male")
                        .setCategory("Smoking")
                        .setSeason("Winter")
                        .setBrand("Армани")
                        .setColor("Чёрный")
                        .setActive(true),
                new Clothes()
                        .setGender("Female")
                        .setCategory("Jacket")
                        .setSeason("Spring")
                        .setBrand("Бен шерман")
                        .setColor("Чёрный")
                        .setActive(true),
                new Clothes()
                        .setGender("Female")
                        .setCategory("Jacket")
                        .setSeason("Spring")
                        .setBrand("Стоне Исланд")
                        .setColor("Зелёный")
                        .setActive(true),
                new Clothes()
                        .setGender("Female")
                        .setCategory("Jeans")
                        .setSeason("Summer")
                        .setBrand("Левайсы")
                        .setColor("Голубой")
                        .setActive(true),
                new Clothes()
                        .setGender("Female")
                        .setCategory("Short")
                        .setSeason("Spring")
                        .setBrand("Бенни")
                        .setColor("Красный")
                        .setActive(true),
                new Clothes()
                        .setGender("Female")
                        .setCategory("Dress")
                        .setSeason("Summer")
                        .setBrand("Армани")
                        .setColor("Жёлтый")
                        .setActive(true)
        );
    }

}

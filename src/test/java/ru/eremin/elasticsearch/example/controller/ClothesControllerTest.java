package ru.eremin.elasticsearch.example.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import ru.eremin.elasticsearch.example.config.ClothesElasticsearchContainer;
import ru.eremin.elasticsearch.example.controller.dto.ClothesSearchRequest;
import ru.eremin.elasticsearch.example.controller.dto.Condition;
import ru.eremin.elasticsearch.example.event.ApplicationStartEvents;
import ru.eremin.elasticsearch.example.service.ClothesElasticsearchService;
import ru.eremin.elasticsearch.example.storage.model.Clothes;
import ru.eremin.elasticsearch.example.storage.repository.ClothesRepository;
import ru.eremin.elasticsearch.example.util.ClothesQueryCreator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ClothesControllerTest {

    private final ClothesElasticsearchContainer container = new ClothesElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:7.12.0");

    @Autowired
    private WebTestClient client;

    @Autowired
    private ReactiveElasticsearchTemplate reactiveElasticsearchTemplate;

    @Autowired
    private ClothesRepository repository;

    @Autowired
    private ClothesElasticsearchService clothesElasticsearchService;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ApplicationStartEvents applicationStartEvents;

    @BeforeAll
    public void init() {
        Mockito.doNothing().when(applicationStartEvents).createClothesIndexIfNotExist();

        container.start();
        assertTrue(container.isRunning());
        clothesElasticsearchService.createIndex().block();
    }

    @AfterEach
    public void clear() {
        reactiveElasticsearchTemplate.delete(ClothesQueryCreator.EMPTY_QUERY, Clothes.class).block();
    }

    @AfterAll
    public void destroy() {
        container.stop();
        assertFalse(container.isRunning());
    }

    @Test
    public void should_find_all_records() {
        List<Clothes> clothes = List.of(
                createDefaultClothes(),
                createDefaultClothes(),
                createDefaultClothes(),
                createDefaultClothes(),
                createDefaultClothes()
        );

        repository.saveAll(clothes).collectList().block();

        client.get()
                .uri("/api/clothes/all")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo("5");
    }

    @Test
    public void should_find_clothes_by_exact_match() throws JsonProcessingException {
        List<Clothes> clothes = List.of(
                createDefaultClothes(), // Должны найтись
                createDefaultClothes(), // Должны найтись
                createDefaultClothes().setBrand("не тот"),
                createDefaultClothes().setCategory("noop"),
                createDefaultClothes().setSeason("noop"),
                new Clothes().setCategory("noop").setGender("noop").setSeason("noop").setBrand("noop").setColor("noop"),
                new Clothes().setCategory("noop").setGender("noop").setSeason("noop").setBrand("noop").setColor("noop"),
                new Clothes().setCategory("noop").setGender("noop").setSeason("noop").setBrand("noop").setColor("noop"),
                new Clothes()
        );

        repository.saveAll(clothes).collectList().block();

        final Condition condition = new Condition()
                .setGender("Female")
                .setCategory("Jacket")
                .setSeason("Spring")
                .setQuery("Большевичка");

        final ClothesSearchRequest request = new ClothesSearchRequest().setConditions(List.of(condition));

        client.post()
                .uri("/api/clothes/search")
                .contentType(APPLICATION_JSON)
                .body(BodyInserters.fromValue(mapper.writeValueAsString(request)))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(exchangeResult -> {
                            ObjectWriter prettyWriter = mapper.writerWithDefaultPrettyPrinter();
                            try {
                                log.info("RESPONSE:\n" + prettyWriter.writeValueAsString(mapper.readTree(exchangeResult.getResponseBody())));
                            } catch (IOException e) {
                                log.error(null, e);
                            }
                        }
                )
                .jsonPath("$.length()").isEqualTo("2")
                .jsonPath("$[0].brand").isEqualTo("Большевичка")
                .jsonPath("$[1].brand").isEqualTo("Большевичка");

    }

    @Test
    public void should_find_clothes_by_exact_match_and_several_conditions() throws JsonProcessingException {
        List<Clothes> clothes = List.of(
                createDefaultClothes().setId("1"), // Должны найтись
                createDefaultClothes().setId("2"), // Должны найтись
                createDefaultClothes().setId("3").setColor("Зелёный"), // Должны найтись
                new Clothes().setId("4").setCategory("testC").setGender("testG").setSeason("testS").setBrand("noop").setColor("Зелёный"), // Должны найтись
                new Clothes().setId("5").setCategory("noop").setGender("noop").setSeason("noop").setBrand("noop").setColor("Жёлтый"), // Должны найтись
                createDefaultClothes().setBrand("не тот"),
                createDefaultClothes().setCategory("noop"),
                createDefaultClothes().setSeason("noop"),
                new Clothes().setCategory("noop").setGender("noop").setSeason("noop").setBrand("noop").setColor("noop"),
                new Clothes().setCategory("noop").setGender("noop").setSeason("noop").setBrand("noop").setColor("noop"),
                new Clothes()
        );

        repository.saveAll(clothes).collectList().block();

        final Condition condition1 = new Condition()
                .setGender("Female")
                .setCategory("Jacket")
                .setSeason("Spring")
                .setQuery("Большевичка");

        final Condition condition2 = new Condition()
                .setGender("testG")
                .setCategory("testC")
                .setSeason("testS")
                .setQuery("Зелёный");

        final Condition condition3 = new Condition()
                .setQuery("Жёлтый");

        final ClothesSearchRequest request = new ClothesSearchRequest().setConditions(List.of(condition1, condition2, condition3));

        client.post()
                .uri("/api/clothes/search")
                .contentType(APPLICATION_JSON)
                .body(BodyInserters.fromValue(mapper.writeValueAsString(request)))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(exchangeResult -> {
                            ObjectWriter prettyWriter = mapper.writerWithDefaultPrettyPrinter();
                            try {
                                log.info("RESPONSE:\n" + prettyWriter.writeValueAsString(mapper.readTree(exchangeResult.getResponseBody())));
                            } catch (IOException e) {
                                log.error(null, e);
                            }
                        }
                )
                .jsonPath("$.length()").isEqualTo("5")
                .jsonPath("$[*].id").value(containsInAnyOrder("1", "2", "3", "4", "5"));

    }

    @ParameterizedTest
    @CsvSource({
            "Бол,лён",
            "<jk,k`y"
    })
    public void should_find_clothes_by_like_query(String brand, String color) throws JsonProcessingException {
        List<Clothes> clothes = List.of(
                createDefaultClothes().setId("1"), // Должны найтись
                createDefaultClothes().setId("2").setBrand("большевичка"), // Должны найтись
                createDefaultClothes().setId("3").setBrand("началобОлконец"), // Должны найтись
                createDefaultClothes().setId("4").setBrand("началоБоЛ"), // Должны найтись
                createDefaultClothes().setId("5").setColor("Зелёный").setBrand("noop"), // Должны найтись
                createDefaultClothes().setId("6").setColor("Зелений").setBrand("noop"), // Должны найтись
                createDefaultClothes().setColor("noop").setBrand("noop"),
                createDefaultClothes().setColor("noop").setBrand("noop"),
                createDefaultClothes().setColor("noop").setBrand("noop"),
                createDefaultClothes().setColor("noop").setBrand("noop"),
                createDefaultClothes().setColor("noop").setBrand("noop"),
                createDefaultClothes().setColor("noop").setBrand("noop"),
                createDefaultClothes().setColor("noop").setBrand("noop")
        );

        repository.saveAll(clothes).collectList().block();

        final Condition condition1 = new Condition()
                .setQuery(brand);

        final Condition condition2 = new Condition()
                .setQuery(color);

        final ClothesSearchRequest request = new ClothesSearchRequest().setConditions(List.of(condition1, condition2));

        client.post()
                .uri("/api/clothes/search")
                .contentType(APPLICATION_JSON)
                .body(BodyInserters.fromValue(mapper.writeValueAsString(request)))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(exchangeResult -> {
                            ObjectWriter prettyWriter = mapper.writerWithDefaultPrettyPrinter();
                            try {
                                log.info("RESPONSE:\n" + prettyWriter.writeValueAsString(mapper.readTree(exchangeResult.getResponseBody())));
                            } catch (IOException e) {
                                log.error(null, e);
                            }
                        }
                )
                .jsonPath("$.length()").isEqualTo("6")
                .jsonPath("$[*].id").value(containsInAnyOrder("1", "2", "3", "4", "5", "6"));
    }

    @Test
    public void should_find_with_smart_search() throws JsonProcessingException {
        List<Clothes> clothes = List.of(
                createDefaultClothes().setId("1"), // Должны найтись
                createDefaultClothes().setId("2").setBrand("Большевичкой"), // Должны найтись
                createDefaultClothes().setId("3").setBrand("Большевичкая"), // Должны найтись
                new Clothes().setId("4").setBrand("noop").setColor("Зелёная"), // Должны найтись
                new Clothes().setId("5").setBrand("noop").setColor("зелёные"), // Должны найтись
                createDefaultClothes().setBrand("не тот"),
                createDefaultClothes().setBrand("не тот").setCategory("noop"),
                createDefaultClothes().setBrand("не тот").setSeason("noop"),
                new Clothes().setCategory("noop").setGender("noop").setSeason("noop").setBrand("noop").setColor("noop"),
                new Clothes().setCategory("noop").setGender("noop").setSeason("noop").setBrand("noop").setColor("noop"),
                new Clothes()
        );

        repository.saveAll(clothes).collectList().block();

        final Condition condition1 = new Condition()
                .setQuery("Большевичка");

        final Condition condition2 = new Condition()
                .setQuery("Зелёный");

        final ClothesSearchRequest request = new ClothesSearchRequest().setConditions(List.of(condition1, condition2));

        client.post()
                .uri("/api/clothes/search")
                .contentType(APPLICATION_JSON)
                .body(BodyInserters.fromValue(mapper.writeValueAsString(request)))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(exchangeResult -> {
                            ObjectWriter prettyWriter = mapper.writerWithDefaultPrettyPrinter();
                            try {
                                log.info("RESPONSE:\n" + prettyWriter.writeValueAsString(mapper.readTree(exchangeResult.getResponseBody())));
                            } catch (IOException e) {
                                log.error(null, e);
                            }
                        }
                )
                .jsonPath("$.length()").isEqualTo("5")
                .jsonPath("$[*].id").value(containsInAnyOrder("1", "2", "3", "4", "5"));
    }

    @Test
    public void should_sort_records() {
        List<Clothes> clothes = new ArrayList<>(List.of(
                createDefaultClothes().setId("1").setCategory("аакатегория").setSeason("аасезон").setBrand("абренд"),
                createDefaultClothes().setId("2").setCategory("аакатегория").setSeason("абсезон").setBrand("абренд"),
                createDefaultClothes().setId("3").setCategory("аакатегория").setSeason("бсезон").setBrand("абренд"),
                createDefaultClothes().setId("4").setCategory("аакатегория").setSeason("бсезон").setBrand("авбренд"),
                createDefaultClothes().setId("5").setCategory("аакатегория").setSeason("бсезон").setBrand("бренд"),
                createDefaultClothes().setId("6").setCategory("аакатегория").setSeason("всезон").setBrand("абренд"),
                createDefaultClothes().setId("7").setCategory("абкатегория").setSeason("всезон").setBrand("абренд"),
                createDefaultClothes().setId("8").setCategory("бкатегория").setSeason("всезон").setBrand("абренд"),
                createDefaultClothes().setId("9").setCategory("якатегория").setSeason("всезон").setBrand("абренд")
        ));

        Collections.shuffle(clothes);
        repository.saveAll(clothes).collectList().block();

        client.get()
                .uri("/api/clothes/all")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(exchangeResult -> {
                            ObjectWriter prettyWriter = mapper.writerWithDefaultPrettyPrinter();
                            try {
                                log.info("RESPONSE:\n" + prettyWriter.writeValueAsString(mapper.readTree(exchangeResult.getResponseBody())));
                            } catch (IOException e) {
                                log.error(null, e);
                            }
                        }
                )
                .jsonPath("$.length()").isEqualTo("9")
                .jsonPath("$[*].id").value(contains("1", "2", "3", "4", "5", "6", "7", "8", "9"));
    }

    @Test
    public void should_find_only_active_records() {
        List<Clothes> clothes = new ArrayList<>(List.of(
                createDefaultClothes().setId("1"),
                createDefaultClothes().setId("2"),
                createDefaultClothes().setId("3"),
                createDefaultClothes().setId("4"),
                createDefaultClothes().setId("5"),
                createDefaultClothes().setId("6").setActive(false),
                createDefaultClothes().setId("7").setActive(false),
                createDefaultClothes().setId("8").setActive(false),
                createDefaultClothes().setId("9").setActive(false)
        ));

        Collections.shuffle(clothes);
        repository.saveAll(clothes).collectList().block();

        client.get()
                .uri("/api/clothes/all")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(exchangeResult -> {
                            ObjectWriter prettyWriter = mapper.writerWithDefaultPrettyPrinter();
                            try {
                                log.info("RESPONSE:\n" + prettyWriter.writeValueAsString(mapper.readTree(exchangeResult.getResponseBody())));
                            } catch (IOException e) {
                                log.error(null, e);
                            }
                        }
                )
                .jsonPath("$.length()").isEqualTo("5")
                .jsonPath("$[*].id").value(containsInAnyOrder("1", "2", "3", "4", "5"));
    }

    @Test
    public void should_search_with_right_fuzziness() throws JsonProcessingException {
        List<Clothes> clothes = new ArrayList<>(List.of(
                createDefaultClothes().setId("1").setBrand("Армани"),
                createDefaultClothes().setId("2").setBrand("Армани"),
                createDefaultClothes().setId("3").setBrand("Армани"),
                createDefaultClothes().setId("4").setBrand("Стон Исланд"),
                createDefaultClothes().setId("5"),
                createDefaultClothes().setId("6").setActive(false),
                createDefaultClothes().setId("7").setActive(false),
                createDefaultClothes().setId("8").setActive(false),
                createDefaultClothes().setId("9").setActive(false)
        ));

        Collections.shuffle(clothes);
        repository.saveAll(clothes).collectList().block();

        final Condition condition1 = new Condition()
                .setQuery("ман");

        final ClothesSearchRequest request = new ClothesSearchRequest().setConditions(List.of(condition1));

        client.post()
                .uri("/api/clothes/search")
                .contentType(APPLICATION_JSON)
                .body(BodyInserters.fromValue(mapper.writeValueAsString(request)))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(exchangeResult -> {
                            ObjectWriter prettyWriter = mapper.writerWithDefaultPrettyPrinter();
                            try {
                                log.info("RESPONSE:\n" + prettyWriter.writeValueAsString(mapper.readTree(exchangeResult.getResponseBody())));
                            } catch (IOException e) {
                                log.error(null, e);
                            }
                        }
                )
                .jsonPath("$.length()").isEqualTo("3")
                .jsonPath("$[*].id").value(containsInAnyOrder("1", "2", "3"));
    }

    private Clothes createDefaultClothes() {
        return new Clothes()
                .setGender("Female")
                .setCategory("Jacket")
                .setSeason("Spring")
                .setBrand("Большевичка")
                .setColor("Красный");
    }
}

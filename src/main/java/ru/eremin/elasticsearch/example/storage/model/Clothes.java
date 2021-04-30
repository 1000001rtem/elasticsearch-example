package ru.eremin.elasticsearch.example.storage.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Document(indexName = "clothes", createIndex = false)
public class Clothes {
    @Id
    private String id = UUID.randomUUID().toString();
    private String gender = null;
    private String category = null;
    private String season = null;
    private String brand = null;
    private String color = null;
    private boolean active = true;
}

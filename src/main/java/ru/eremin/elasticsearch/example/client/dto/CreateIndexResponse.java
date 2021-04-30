package ru.eremin.elasticsearch.example.client.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class CreateIndexResponse {
    private String index;
}

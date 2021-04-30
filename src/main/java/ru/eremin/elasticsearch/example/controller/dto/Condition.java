package ru.eremin.elasticsearch.example.controller.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class Condition {
    private String gender;
    private String category;
    private String season;
    private String query;
}

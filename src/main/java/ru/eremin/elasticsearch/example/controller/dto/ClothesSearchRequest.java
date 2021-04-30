package ru.eremin.elasticsearch.example.controller.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class ClothesSearchRequest {
    private List<Condition> conditions;
}

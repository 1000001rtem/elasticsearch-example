package ru.eremin.elasticsearch.example.util;

import lombok.NoArgsConstructor;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import ru.eremin.elasticsearch.example.controller.dto.Condition;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public class ClothesQueryCreator {

    private static final List<FieldSortBuilder> DEFAULT_SORTING = List.of(
            SortBuilders.fieldSort("category").order(SortOrder.ASC),
            SortBuilders.fieldSort("season").order(SortOrder.ASC),
            //в эластике поля с типом text не сортируются.
            // Поэтому применяется multiField(делаем поле с типом keyword внутри поля).
            // см. раздел mapping в настройках индекса
            SortBuilders.fieldSort("brand.raw").order(SortOrder.ASC)
    );
    private static final BoolQueryBuilder BASE_QUERY = QueryBuilders.boolQuery()
            .must(QueryBuilders.termQuery("active", true));

    public static final NativeSearchQuery EMPTY_QUERY;

    static {
        final NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder()
                .withQuery(BASE_QUERY);
        DEFAULT_SORTING.forEach(builder::withSort);
        EMPTY_QUERY = builder.build();
    }

    private Pageable pageable = PageRequest.of(0, 100);
    private final List<FieldSortBuilder> sort = DEFAULT_SORTING;
    private final List<Condition> conditions = new ArrayList<>();

    public ClothesQueryCreator addCondition(Condition condition) {
        this.conditions.add(condition);
        return this;
    }

    public ClothesQueryCreator addConditions(List<Condition> conditions) {
        this.conditions.addAll(conditions);
        return this;
    }

    public ClothesQueryCreator addSort(FieldSortBuilder sort) {
        this.sort.add(sort);
        return this;
    }

    public ClothesQueryCreator setPageable(Pageable pageable) {
        this.pageable = pageable;
        return this;
    }

    public NativeSearchQuery createQuery() {
        final NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();
        builder.withQuery(createSearchQuery())
                .withPageable(this.pageable);
        this.sort.forEach(builder::withSort);
        return builder.build();
    }

    private QueryBuilder createSearchQuery() {
        final BoolQueryBuilder conditionQuery = QueryBuilders.boolQuery();
        this.conditions.forEach(condition -> conditionQuery.should(convertCondition(condition)));
        return QueryBuilders.boolQuery().must(conditionQuery).must(BASE_QUERY);
    }

    private BoolQueryBuilder convertCondition(Condition condition) {
        final BoolQueryBuilder builder = QueryBuilders.boolQuery();
        if (condition.getGender() != null) {
            builder.must(QueryBuilders.matchQuery("gender", condition.getGender()));
        }
        if (condition.getCategory() != null) {
            builder.must(QueryBuilders.matchQuery("category", condition.getCategory()));
        }
        if (condition.getSeason() != null) {
            builder.must(QueryBuilders.matchQuery("season", condition.getSeason()));
        }
        if (condition.getQuery() != null) {
            builder.must(
                    QueryBuilders.boolQuery()
                            .should(
                                    QueryBuilders.multiMatchQuery(condition.getQuery(), "brand", "color")
                            )
                            // для ошибок ракладки клавиатуры
                            .should(
                                    QueryBuilders.multiMatchQuery(condition.getQuery(), "brand", "color")
                                            .analyzer("ru_en_char_analyzer")
                            )
            );
        }
        return builder;
    }
}

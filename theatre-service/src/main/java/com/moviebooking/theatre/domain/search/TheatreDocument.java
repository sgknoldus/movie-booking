package com.moviebooking.theatre.domain.search;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.GeoPointField;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

import java.util.List;
import java.util.UUID;

@Document(indexName = "theatres")
@Data
@Builder
public class TheatreDocument {
    @Id
    private UUID id;

    @Field(type = FieldType.Text)
    private String name;

    @Field(type = FieldType.Keyword)
    private String city;

    @Field(type = FieldType.Text)
    private String address;

    @GeoPointField
    private GeoPoint location;

    @Field(type = FieldType.Nested)
    private List<MovieShow> movies;

    @Data
    @Builder
    public static class MovieShow {
        @Field(type = FieldType.Keyword)
        private UUID movieId;

        @Field(type = FieldType.Date)
        private List<String> showTimes;
    }
}

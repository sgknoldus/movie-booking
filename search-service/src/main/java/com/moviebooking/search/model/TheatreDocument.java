package com.moviebooking.search.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.GeoPointField;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

import java.time.LocalDateTime;

@Document(indexName = "theatres")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TheatreDocument {
    @Id
    private String id;

    @Field(type = FieldType.Long)
    private Long theatreId;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String name;
    
    @Field(type = FieldType.Text)
    private String address;
    
    @Field(type = FieldType.Keyword)
    private String phoneNumber;
    
    @Field(type = FieldType.Keyword)
    private String email;
    
    @GeoPointField
    private GeoPoint location;
    
    @Field(type = FieldType.Long)
    private Long cityId;
    
    @Field(type = FieldType.Text, analyzer = "standard")
    private String cityName;
    
    @Field(type = FieldType.Text)
    private String cityState;
    
    @Field(type = FieldType.Text)
    private String cityCountry;
    
    @Field(type = FieldType.Date, format = {}, pattern = "uuuu-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @Field(type = FieldType.Date, format = {}, pattern = "uuuu-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
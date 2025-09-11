package com.moviebooking.search.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

@Document(indexName = "cities")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CityDocument {
    @Id
    private String id;
    
    @Field(type = FieldType.Text, analyzer = "standard")
    private String name;
    
    @Field(type = FieldType.Text)
    private String state;
    
    @Field(type = FieldType.Text)
    private String country;
    
    @Field(type = FieldType.Keyword)
    private String zipCode;
    
    @Field(type = FieldType.Date, format = {}, pattern = "uuuu-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @Field(type = FieldType.Date, format = {}, pattern = "uuuu-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
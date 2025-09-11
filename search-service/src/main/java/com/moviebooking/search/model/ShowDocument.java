package com.moviebooking.search.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(indexName = "shows")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShowDocument {
    @Id
    private String id;
    
    @Field(type = FieldType.Long)
    private Long movieId;
    
    @Field(type = FieldType.Text, analyzer = "standard")
    private String movieTitle;
    
    @Field(type = FieldType.Date)
    private LocalDateTime showDateTime;
    
    @Field(type = FieldType.Date)
    private LocalDateTime endDateTime;
    
    @Field(type = FieldType.Double)
    private BigDecimal price;
    
    @Field(type = FieldType.Integer)
    private Integer availableSeats;
    
    @Field(type = FieldType.Keyword)
    private String status;
    
    @Field(type = FieldType.Long)
    private Long screenId;
    
    @Field(type = FieldType.Text, analyzer = "standard")
    private String screenName;
    
    @Field(type = FieldType.Keyword)
    private String screenType;
    
    @Field(type = FieldType.Long)
    private Long theatreId;
    
    @Field(type = FieldType.Text, analyzer = "standard")
    private String theatreName;
    
    @Field(type = FieldType.Long)
    private Long cityId;
    
    @Field(type = FieldType.Text, analyzer = "standard")
    private String cityName;
    
    @Field(type = FieldType.Date)
    private LocalDateTime createdAt;
    
    @Field(type = FieldType.Date)
    private LocalDateTime updatedAt;
}
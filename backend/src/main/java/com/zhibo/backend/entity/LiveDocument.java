package com.zhibo.backend.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@Document(indexName = "live")
public class LiveDocument {
    @Id
    private Long id;
    
    @Field(type = FieldType.Text)
    private String title;
    
    @Field(type = FieldType.Text)
    private String description;
    
    @Field(type = FieldType.Keyword)
    private Long userId;
    
    @Field(type = FieldType.Keyword)
    private String userNickname;
    
    @Field(type = FieldType.Keyword)
    private Long categoryId;
    
    @Field(type = FieldType.Keyword)
    private String categoryName;
    
    @Field(type = FieldType.Integer)
    private Integer status;
    
    @Field(type = FieldType.Long)
    private Long viewCount;
    
    @Field(type = FieldType.Keyword)
    private String cover;
    
    @Field(type = FieldType.Date)
    private String startTime;
    
    @Field(type = FieldType.Date)
    private String createdAt;
}

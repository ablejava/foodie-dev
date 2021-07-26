package com.ablejava.pojo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * @Author: xiazhongwei
 * @Date: 2021/7/26 21:36
 */
@Document(indexName = "sut", type = "_doc")
@Data
public class Stu {
    @Id
    private Long stuId;

    @Field(store = true)
    private String name;

    @Field(store = true)
    private Integer age;

    @Field(store = true, type = FieldType.Keyword)
    private String sign;

    @Field(store = true)
    private String desc;
}

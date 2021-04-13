package com.chryl.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;

/**
 * Created by Chr.yl on 2021/4/8.
 *
 * @author Chr.yl
 */
@Data
@Document(indexName = "chr_company", type = "company", shards = 1, replicas = 0)
public class ChrCompany implements Serializable {

    private static final long serialVersionUID = 7083418848729758708L;

    @Id
    private Long companyId;

    @Field(analyzer = "ik_max_word", type = FieldType.Text)
    private String companyName;

    @Field(type = FieldType.Integer)
    private Integer companyCode;

    @Field(analyzer = "ik_max_word", type = FieldType.Text)
    private String companyDescription;

    @Field(analyzer = "ik_max_word", type = FieldType.Text)//中文分词
    private String companyChinese;

}

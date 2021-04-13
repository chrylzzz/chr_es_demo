package com.chryl.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Chr.yl on 2021/4/8.
 *
 * @author Chr.yl
 */
@Data
@Document(indexName = "chr_company_emp", type = "company_emp", shards = 1, replicas = 0)
public class ChrCompanyEmp implements Serializable {

    private static final long serialVersionUID = 7083418848729758708L;

    @Id
    private Long companyId;

    @Field(analyzer = "ik_max_word", type = FieldType.Text)//中文分词
    private String companyName;

    private Integer companyCode;

    @Field(analyzer = "ik_max_word", type = FieldType.Text)//中文分词
//    @Field(type = FieldType.Keyword)//不会进行分词建立索引的类型
    private String companyDescription;

    @Field(analyzer = "ik_max_word", type = FieldType.Text)//中文分词
//    @Field(type = FieldType.Keyword)//不会进行分词建立索引的类型
    private String companyChinese;

    //嵌套的对象
    @Field(type = FieldType.Nested)
    private List<ChrEmp> chrEmpList;

}

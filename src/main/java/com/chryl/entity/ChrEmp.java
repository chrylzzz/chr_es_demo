package com.chryl.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

/**
 * Created by Chr.yl on 2021/4/8.
 *
 * @author Chr.yl
 */
@Data
@Document(indexName = "chr_emp", type = "emp", shards = 1, replicas = 0)
public class ChrEmp implements Serializable {

    private static final long serialVersionUID = -7333445576083396574L;


    //    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)//只可以操作,不可以返回
    @Id
    private Long empId;

    @Field(analyzer = "ik_max_word", type = FieldType.Text)
    private String empName;


    /**
     * es 存时间问题未解决,
     * es long存入如何模糊查询未解决,
     * es 查询中文未解决,
     */
//    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "GMT+8")
//    @Field(type = FieldType.Date, format = DateFormat.custom, pattern = "yyyy-MM-dd")
//    @Field(type = FieldType.Date, format = DateFormat.custom, pattern = "yyyy-MM-dd HH:mm:ss:SSS")
//    @Field(type = FieldType.Date, format = DateFormat.custom, pattern = "yyyy-MM-dd HH:mm:ss")
//    private LocalDate empDate;//long节约空间
//    private Date empDate;

//    @Field(type = FieldType.Double)
    private BigDecimal empSal;

    @Field(type = FieldType.Long)
    private Long companyId;

    @Field(type = FieldType.Long)
    private Long empIdcard;

    @Field(type = FieldType.Integer)
    private Integer empCode;

    @Field(type = FieldType.Text)
    private String empStrDate;

    @Field(analyzer = "ik_max_word", type = FieldType.Text)
    private String empRealName;
}

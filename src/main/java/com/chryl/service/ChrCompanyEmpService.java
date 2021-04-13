package com.chryl.service;

import com.chryl.entity.ChrCompanyEmp;
import com.chryl.mapper.ChrCompanyEmpMapper;
import com.chryl.repository.ChrCompanyEmpRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.lucene.search.function.FunctionScoreQuery;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by Chr.yl on 2021/4/8.
 *
 * @author Chr.yl
 */
@Service
@Slf4j
public class ChrCompanyEmpService {

    @Autowired
    private ChrCompanyEmpMapper chrCompanyEmpMapper;

    @Autowired
    private ChrCompanyEmpRepository chrCompanyEmpRepository;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    /**
     * 查询mysql所有数据
     *
     * @return
     */
    public List<ChrCompanyEmp> getCompanyEmpAll() {
        return chrCompanyEmpMapper.selectCompanyEmpAll();
    }

    /**
     * 数据导入es
     *
     * @return
     */
    public int importCompanyEmpAll() {
        List<ChrCompanyEmp> chrCompanyEmpList = chrCompanyEmpMapper.selectCompanyEmpAll();
        Iterable<ChrCompanyEmp> chrCompanyEmpIterable = chrCompanyEmpRepository.saveAll(chrCompanyEmpList);
        Iterator<ChrCompanyEmp> iterator = chrCompanyEmpIterable.iterator();
        int result = 1;
        while (iterator.hasNext()) {
            result++;
            iterator.next();
        }
        return result;
    }


    /**
     * 查询indexName是否存在
     *
     * @param indexName
     * @return
     */
    public Boolean indexExists(String indexName) {
        return elasticsearchTemplate.indexExists(indexName);
    }


    /**
     * 查询
     *
     * @param keyword
     * @param companyId
     * @param empId
     * @param sort
     * @return
     */
    public Page<ChrCompanyEmp> search(String keyword, Long companyId, Long empId, Integer page, Integer size, Integer sort) {
        //分页
        Pageable pageable = PageRequest.of(page, size);
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();

        nativeSearchQueryBuilder.withPageable(pageable);

        //如果查询的是id,那么就精确查询
        if (companyId != null || empId != null) {
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            /**
             * 组合查询BoolQueryBuilder:builder下有must、should以及mustNot 相当于sql中的and、or以及not
             * must(QueryBuilders)   :AND
             * mustNot(QueryBuilders):NOT
             * should(QueryBuilders):OR
             */
            if (companyId != null) {
                //精确查询:termQuery
                boolQueryBuilder.must(QueryBuilders.termQuery("companyId", companyId));
            }
            if (empId != null) {
                boolQueryBuilder.must(QueryBuilders.termQuery("empId", empId));
            }
            //模糊查询:wildcardQuery
//            boolQueryBuilder.must(QueryBuilders.wildcardQuery("companyName", "*oo*"));


            nativeSearchQueryBuilder.withFilter(boolQueryBuilder);
        }

        if (StringUtils.isBlank(keyword)) {
            //相当于就没有设置查询条件
            nativeSearchQueryBuilder.withQuery(QueryBuilders.matchAllQuery());
        } else {
            /**
             fuzzyQuery 设置模糊搜索,有学习两个字
             builder.must(QueryBuilders.fuzzyQuery("sumary", "学习"));

             设置要查询的内容中含有关键字
             builder.must(new QueryStringQueryBuilder("man").field("springdemo"));

             matchQuery 分词查询，--这里可以设置分词器,采用默认的分词器
             */
            //分词查询
            List<FunctionScoreQueryBuilder.FilterFunctionBuilder> filterFunctionBuilders = new ArrayList<>();
            filterFunctionBuilders.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                    QueryBuilders.matchQuery("companyName", keyword),//分词查询
                    ScoreFunctionBuilders.weightFactorFunction(10)////设置权重
            ));
            filterFunctionBuilders.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                    QueryBuilders.matchQuery("empName", keyword),
                    ScoreFunctionBuilders.weightFactorFunction(5)
            ));
            filterFunctionBuilders.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                    QueryBuilders.matchQuery("companyDescription", keyword),//分词查询
                    ScoreFunctionBuilders.weightFactorFunction(2)////设置权重
            ));


            FunctionScoreQueryBuilder.FilterFunctionBuilder[] builders = new FunctionScoreQueryBuilder.FilterFunctionBuilder[filterFunctionBuilders.size()];
            filterFunctionBuilders.toArray(builders);

            //build query
            FunctionScoreQueryBuilder functionScoreQueryBuilder = QueryBuilders.functionScoreQuery(builders)
                    .scoreMode(FunctionScoreQuery.ScoreMode.SUM)
                    .setMinScore(2);

            //写入query
            nativeSearchQueryBuilder.withQuery(functionScoreQueryBuilder);
        }

        /**

         org.elasticsearch.index.query.QueryShardException: No mapping found for [empDate] in order to sort on
         org.elasticsearch.index.query.QueryShardException: No mapping found for [empId] in order to sort on


         Elasticsearch对排序、聚合所依据的字段用单独的数据结构(fielddata)缓存到内存里了，但是在text字段上默认是禁用的，如果有需要单独开启，这样做的目的是为了节省内存空间。所以如果需要进行聚合操作，需要单独开启。
         java.lang.IllegalArgumentException: Fielddata is disabled on text fields by default. Set fielddata=true on [companyName] in order to load fielddata in memory by uninverting the inverted index. Note that this can however use significant memory. Alternatively use a keyword field instead.


         */
        if (sort == 1) {//long
            nativeSearchQueryBuilder.withSort(SortBuilders.fieldSort("companyId").order(SortOrder.DESC));
        } else if (sort == 2) {//text不默认不开启排序
            nativeSearchQueryBuilder.withSort(SortBuilders.fieldSort("companyName").order(SortOrder.DESC));
        } else if (sort == 3) {//int
            nativeSearchQueryBuilder.withSort(SortBuilders.fieldSort("companyCode").order(SortOrder.DESC));
        } else if (sort == 6) {//long
            nativeSearchQueryBuilder.withSort(SortBuilders.fieldSort("empId").order(SortOrder.ASC));
        } else if (sort == 4) {//big decimal
            nativeSearchQueryBuilder.withSort(SortBuilders.fieldSort("empSal").order(SortOrder.ASC));
        } else if (sort == 5) {//date
            nativeSearchQueryBuilder.withSort(SortBuilders.fieldSort("empDate").order(SortOrder.ASC));
        }

        //自定义排序规则
        nativeSearchQueryBuilder.withSort(SortBuilders.scoreSort().order(SortOrder.DESC));
        //将build query放入search query
        NativeSearchQuery searchQuery = nativeSearchQueryBuilder.build();
        log.info("DSL:{}", searchQuery.getQuery().toString());
        //执行查询
        return chrCompanyEmpRepository.search(searchQuery);

    }


    //精确查找
    public Page<ChrCompanyEmp> findByCompanyCodeOrCompanyNameOrCompanyDescription(Integer companyCode, String companyName, String companyDescription, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        return chrCompanyEmpRepository.findByCompanyCodeOrCompanyNameOrCompanyDescription(companyCode, companyName, companyDescription, pageable);
    }

    //模糊查询: like
    public Page<ChrCompanyEmp> findByCompanyNameLikeOrCompanyDescriptionLike(String companyName, String companyDescription, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        return chrCompanyEmpRepository.findByCompanyNameLikeOrCompanyDescriptionLike(companyName, companyDescription, pageable);
    }

    //包含查询 contains
    public Page<ChrCompanyEmp> findByCompanyNameContainsOrCompanyDescriptionContains(String companyName, String companyDescription, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        return chrCompanyEmpRepository.findByCompanyNameContainsOrCompanyDescriptionContains(companyName, companyDescription, pageable);
    }


    //
    public List<ChrCompanyEmp> searchQuery(String keyword, Integer page, Integer size) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.should(QueryBuilders.wildcardQuery("companyName", keyword + "*"));
        boolQueryBuilder.should(QueryBuilders.wildcardQuery("companyDescription", keyword + "*"));
        Map<String, Float> fieldMap = new HashMap<>();
        fieldMap.put("companyName", 2F);
        fieldMap.put("companyDescription", 3F);
        fieldMap.put("empName", 4F);
        //创建一个SearchQuery对象
        //设置查询条件，此处可以使用QueryBuilders创建多种查询
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
//                .withQuery(QueryBuilders.queryStringQuery("备份节点上没有数据").defaultField("title"))
                //精确查询,单字符串
//                .withQuery(QueryBuilders.queryStringQuery(keyword).defaultField("companyName"))
//                .withQuery(QueryBuilders.queryStringQuery(keyword).field("companyName"))
                //没搞懂------
//                .withQuery(QueryBuilders.queryStringQuery(keyword).fields(fieldMap))
//                .withQuery(QueryBuilders.queryStringQuery(keyword).fuzziness())
                //没搞懂------

                //不分词,精确查询,若完全匹配，则可查询到。
//                .withQuery(QueryBuilders.termQuery("companyName", keyword))
                //分词查询
//                .withQuery(QueryBuilders.matchQuery("companyName", keyword))
                //分词模糊查询
//                .withQuery(QueryBuilders.fuzzyQuery("companyName", keyword))
                //通配符查询，支持* 任意字符串；？任意一个字符
//                .withQuery(QueryBuilders.wildcardQuery("companyName", keyword + "*"))
                //范围查询
//                .withQuery(QueryBuilders.rangeQuery("companyId").lt(keyword))

                //多条件查询
                .withQuery(boolQueryBuilder)


                //还可以设置分页信息
                .withPageable(PageRequest.of(page, size))
                //创建SearchQuery对象
                .build();

        return elasticsearchTemplate.queryForList(searchQuery, ChrCompanyEmp.class);
    }

    //多条件模糊查询
    public List<ChrCompanyEmp> searchChinesQuery(Integer companyCode, String companyName, String companyChinese, String companyDescription, Integer page, Integer size) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        if (companyCode != null) {//数值查询未解决
//            boolQueryBuilder.should(QueryBuilders.wildcardQuery("companyCode", companyCode + "*"));
        }
        if (companyName != null) {
            boolQueryBuilder.should(QueryBuilders.wildcardQuery("companyName", companyName + "*"));
        }
        if (companyChinese != null) {
            boolQueryBuilder.should(QueryBuilders.wildcardQuery("companyChinese", companyChinese + "*"));
        }
        if (companyDescription != null) {
            boolQueryBuilder.should(QueryBuilders.wildcardQuery("companyDescription", companyDescription + "*"));
        }

        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                //多条件查询
                .withQuery(boolQueryBuilder)
                //还可以设置分页信息
                .withPageable(PageRequest.of(page, size))
                //创建SearchQuery对象
                .build();
        return elasticsearchTemplate.queryForList(searchQuery, ChrCompanyEmp.class);
    }


    public void index() {
        IndexQuery indexQuery = new IndexQuery();
        indexQuery.setIndexName("chr_company_emp");
        String index = elasticsearchTemplate.index(indexQuery);
    }

    public Object count() {
        Criteria criteria = new Criteria();
        criteria.and("empId");
        CriteriaQuery criteriaQuery = new CriteriaQuery(criteria);
        return elasticsearchTemplate.count(criteriaQuery);
    }

}

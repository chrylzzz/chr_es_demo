package com.chryl.service;

import com.alibaba.fastjson.JSON;
import com.chryl.entity.ChrCompanyEmp;
import com.chryl.mapper.ChrCompanyEmpMapper;
import com.chryl.repository.ChrCompanyEmpRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.lucene.search.function.FunctionScoreQuery;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.*;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.aggregations.bucket.filter.FilterAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.filter.FiltersAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.filter.FiltersAggregator;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.avg.AvgAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.max.MaxAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.min.MinAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.sum.SumAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.valuecount.ValueCountAggregationBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
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
     * 查询,
     * 包括嵌套属性查询;
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
                /**
                 * empId为嵌套内对象的属性,不与外属性查询方法查询
                 */
                //方法1,直接构建query
//                boolQueryBuilder.must(QueryBuilders.termQuery("empId", empId));
                boolQueryBuilder.must(QueryBuilders.nestedQuery(
                        "chrEmpList",//嵌套变量名名
                        new TermQueryBuilder("chrEmpList.empId", empId),//查询字段,值
                        ScoreMode.None));

                //方法2,对象构建query
//                NestedQueryBuilder nestedQuery = new NestedQueryBuilder("chrEmpList",//nested嵌套的变量名
//                        new TermQueryBuilder("chrEmpList.empId", empId),//查询的属性字段,值
//                        ScoreMode.None);
//                boolQueryBuilder.must(nestedQuery);
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
             fuzzyQuery 设置模糊搜索,有学习两个字,类似于包含
             builder.must(QueryBuilders.fuzzyQuery("sumary", "学习"));

             wildcardQuery 模糊查询 ,类似于like查询
             builder.must(QueryBuilders.fuzzyQuery("sumary", "学习"+"*"));

             设置要查询的内容中含有关键字
             builder.must(new QueryStringQueryBuilder("man").field("springdemo"));

             matchQuery 分词查询，--这里可以设置分词器,采用默认的分词器


             */
            //分词查询
            List<FunctionScoreQueryBuilder.FilterFunctionBuilder> filterFunctionBuilders = new ArrayList<>();
            filterFunctionBuilders.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(
//                    QueryBuilders.matchQuery("companyName", keyword),//分词查询
//                    QueryBuilders.wildcardQuery("companyName", keyword + "*"),//全英文,不分词查询,模糊查询,类似于like查询
                    QueryBuilders.fuzzyQuery("companyName", keyword),//全英文,不分词查询,模糊查询,类似于包含查询contains
                    ScoreFunctionBuilders.weightFactorFunction(10)////设置权重
            ));
            //嵌套
            filterFunctionBuilders.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                    QueryBuilders.nestedQuery("chrEmpList",
                            new MatchQueryBuilder("chrEmpList.empName", keyword), ScoreMode.None),
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
        //排序
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
                //嵌套内属性查询
                .withQuery(QueryBuilders.nestedQuery("chrEmpList",
                        QueryBuilders.boolQuery()//多条件查询
                                .should(QueryBuilders.matchQuery("chrEmpList.empId", keyword))//嵌套属性,值
                                .should(QueryBuilders.matchQuery("chrEmpList.empCode", keyword))
                        , ScoreMode.None)
                )


                //多条件查询
                .withQuery(boolQueryBuilder)
                //还可以设置分页信息
                .withPageable(PageRequest.of(page, size))
                //创建SearchQuery对象
                .build();

        return elasticsearchTemplate.queryForList(searchQuery, ChrCompanyEmp.class);
    }

    //多条件模糊查询
    public List<ChrCompanyEmp> searchChinesQuery(Integer companyCode, Long companyId, Long empId, Integer empCode, String companyName, String companyChinese, String companyDescription, Integer page, Integer size) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        if (companyCode != null) {
            /**
             * 数值查询未解决:
             * 据我测试,数值暂时测试不能用模糊查询等,现在只能进行精确匹配
             */
//            boolQueryBuilder.should(QueryBuilders.wildcardQuery("companyCode", companyCode + "*"));//模糊查询不支持数值
            boolQueryBuilder.should(QueryBuilders.termQuery("companyCode", companyCode));//精确匹配
        }
        if (companyId != null) {//数值精确查询
            boolQueryBuilder.should(QueryBuilders.termQuery("companyId", companyId));
        }
        if (empId != null) {//数值精确查询,嵌套属性查询
            boolQueryBuilder.should(QueryBuilders.nestedQuery("chrEmpList", QueryBuilders.termQuery("chrEmpList.empId", empId), ScoreMode.None));
        }
        if (empCode != null) {//数值精确查询,嵌套属性查询
            boolQueryBuilder.should(QueryBuilders.nestedQuery("chrEmpList", QueryBuilders.termQuery("chrEmpList.empCode", empCode), ScoreMode.None));
        }


        if (companyName != null) {//模糊查询
            boolQueryBuilder.should(QueryBuilders.wildcardQuery("companyName", companyName + "*"));
        }
        if (companyChinese != null) {//模糊查询
            boolQueryBuilder.should(QueryBuilders.wildcardQuery("companyChinese", companyChinese + "*"));
        }
        if (companyDescription != null) {//模糊查询
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


    /**
     * 判断索引是否存在
     *
     * @return boolean
     */
    public boolean indexExists() {
        return elasticsearchTemplate.indexExists(ChrCompanyEmp.class);
    }

    /**
     * 判断索引是否存在
     *
     * @param indexName 索引名称
     * @return boolean
     */
    public boolean indexExists(String indexName) {
        return elasticsearchTemplate.indexExists(indexName);
    }

    /**
     * 创建索引（推荐使用：因为Java对象已经通过注解描述了Setting和Mapping）
     *
     * @return boolean
     */
    public boolean indexCreate() {
        return elasticsearchTemplate.createIndex(ChrCompanyEmp.class);
    }

    /**
     * 创建索引
     *
     * @param indexName 索引名称
     * @return boolean
     */
    public boolean indexCreate(String indexName) {
        return elasticsearchTemplate.createIndex(indexName);
    }

    /**
     * 索引删除
     *
     * @param indexName 索引名称
     * @return boolean
     */
    public boolean indexDelete(String indexName) {
        return elasticsearchTemplate.deleteIndex(indexName);
    }

    /**
     * 新增数据
     *
     * @param bean 数据对象
     */
    public void save(ChrCompanyEmp bean) {
        chrCompanyEmpRepository.save(bean);
    }

    /**
     * 批量新增数据
     *
     * @param list 数据集合
     */
    public void saveAll(List<ChrCompanyEmp> list) {
        chrCompanyEmpRepository.saveAll(list);
    }

    /**
     * 修改数据
     *
     * @param indexName 索引名称
     * @param type      索引类型
     * @param bean      修改数据对象，ID不能为空
     */
    public void update(String indexName, String type, ChrCompanyEmp bean) {
        UpdateRequest updateRequest = new UpdateRequest();
        updateRequest.retryOnConflict(1);//冲突重试
//        updateRequest.doc(JSONUtil.toJsonStr(bean), XContentType.JSON);
        updateRequest.doc(JSON.toJSONString(bean), XContentType.JSON);
//        updateRequest.routing(bean.getId());//默认是_id来路由的，用来路由到不同的shard，会对这个值做hash，然后映射到shard。所以分片
        updateRequest.routing(String.valueOf(bean.getCompanyId()));//默认是_id来路由的，用来路由到不同的shard，会对这个值做hash，然后映射到shard。所以分片
//        UpdateQuery query = new UpdateQueryBuilder().withIndexName(indexName).withType(type).withId(bean.getId())
        UpdateQuery query = new UpdateQueryBuilder().withIndexName(indexName).withType(type).withId(String.valueOf(bean.getCompanyId()))
                .withDoUpsert(true)//不加默认false。true表示更新时不存在就插入
                .withClass(ChrCompanyEmp.class).withUpdateRequest(updateRequest).build();
        UpdateResponse updateResponse = elasticsearchTemplate.update(query);
    }

    /**
     * 根据ID，删除数据
     *
     * @param id 数据ID
     */
//    public void deleteById(String id) {
    public void deleteById(Long id) {
        chrCompanyEmpRepository.deleteById(id);
    }

    /**
     * 根据对象删除数据，主键ID不能为空
     *
     * @param bean 对象
     */
    public void deleteByBean(ChrCompanyEmp bean) {
        chrCompanyEmpRepository.delete(bean);
    }

    /**
     * 根据对象集合，批量删除
     *
     * @param beanList 对象集合
     */
    public void deleteAll(List<ChrCompanyEmp> beanList) {
        chrCompanyEmpRepository.deleteAll(beanList);
    }

    /**
     * 删除所有
     */
    public void deleteAll() {
        chrCompanyEmpRepository.deleteAll();
    }

    /**
     * 根据条件，自定义删除（在setQuery中的条件，可以根据需求自由拼接各种参数，与查询方法一样）
     *
     * @param indexName 索引
     * @param type      索引类型
     */
    public void delete(String indexName, String type) {
        DeleteQuery deleteQuery = new DeleteQuery();
        deleteQuery.setIndex(indexName);
        deleteQuery.setType(type);//建index没配置就是类名全小写
        deleteQuery.setQuery(new BoolQueryBuilder().must(QueryBuilders.termQuery("mobile", "13526568454")));
        elasticsearchTemplate.delete(deleteQuery);
    }

    /**
     * 批量新增
     *
     * @param indexName 索引名称
     * @param type      索引类型
     * @param beanList  新增对象集合
     */
    public void batchSave(String indexName, String type, List<ChrCompanyEmp> beanList) {
        List<IndexQuery> queries = new ArrayList<>();
        IndexQuery indexQuery;
        int counter = 0;
        for (ChrCompanyEmp item : beanList) {
            indexQuery = new IndexQuery();
//            indexQuery.setId(item.getId());
            indexQuery.setId(String.valueOf(item.getCompanyId()));
//            indexQuery.setSource(JSONUtil.toJsonStr(item));
            indexQuery.setSource(JSON.toJSONString(item));
            indexQuery.setIndexName(indexName);
            indexQuery.setType(type);
            queries.add(indexQuery);
            //分批提交索引
            if (counter != 0 && counter % 1000 == 0) {
                elasticsearchTemplate.bulkIndex(queries);
                queries.clear();
                System.out.println("bulkIndex counter : " + counter);
            }
            counter++;
        }
        //不足批的索引最后不要忘记提交
        if (queries.size() > 0) {
            elasticsearchTemplate.bulkIndex(queries);
        }
        elasticsearchTemplate.refresh(indexName);
    }

    /**
     * 批量修改
     *
     * @param indexName 索引名称
     * @param type      索引类型
     * @param beanList  修改对象集合
     */
    public void batchUpdate(String indexName, String type, List<ChrCompanyEmp> beanList) {
        List<UpdateQuery> queries = new ArrayList<>();
        UpdateQuery updateQuery;
        UpdateRequest updateRequest;
        int counter = 0;
        for (ChrCompanyEmp item : beanList) {
            updateRequest = new UpdateRequest();
            updateRequest.retryOnConflict(1);//冲突重试
            updateRequest.doc(item);
//            updateRequest.routing(item.getId());
            updateRequest.routing(String.valueOf(item.getCompanyId()));

            updateQuery = new UpdateQuery();
//            updateQuery.setId(item.getId());
            updateQuery.setId(String.valueOf(item.getCompanyId()));
            updateQuery.setDoUpsert(true);
            updateQuery.setUpdateRequest(updateRequest);
            updateQuery.setIndexName(indexName);
            updateQuery.setType(type);
            queries.add(updateQuery);
            //分批提交索引
            if (counter != 0 && counter % 1000 == 0) {
                elasticsearchTemplate.bulkUpdate(queries);
                queries.clear();
                System.out.println("bulkIndex counter : " + counter);
            }
            counter++;
        }
        //不足批的索引最后不要忘记提交
        if (queries.size() > 0) {
            elasticsearchTemplate.bulkUpdate(queries);
        }
        elasticsearchTemplate.refresh(indexName);
    }

    /**
     * 数据查询，返回List
     *
     * @param field 查询字段
     * @param value 查询值
     * @return List<ChrCompanyEmp>
     */
    public List<ChrCompanyEmp> queryMatchList(String field, String value) {
        MatchQueryBuilder builder = QueryBuilders.matchQuery(field, value);
        SearchQuery searchQuery = new NativeSearchQuery(builder);
        return elasticsearchTemplate.queryForList(searchQuery, ChrCompanyEmp.class);
    }

    /**
     * 数据查询，返回Page
     *
     * @param field 查询字段
     * @param value 查询值
     * @return AggregatedPage<ChrCompanyEmp>
     */
//    public AggregatedPage<ChrCompanyEmp> queryMatchPage(String field, String value) {
//        MatchQueryBuilder builder = QueryBuilders.matchQuery(field, value);
//        SearchQuery searchQuery = new NativeSearchQuery(builder).setPageable(PageRequest.of(0, 100));
//        AggregatedPage<ChrCompanyEmp> page = elasticsearchTemplate.queryForPage(searchQuery, ChrCompanyEmp.class);
//
//        long totalElements = page.getTotalElements(); // 总记录数
//        int totalPages = page.getTotalPages();  // 总页数
//        int pageNumber = page.getPageable().getPageNumber(); // 当前页号
//        List<ChrCompanyEmp> beanList = page.toList();  // 当前页数据集
//        Set<ChrCompanyEmp> beanSet = page.toSet();  // 当前页数据集
//        return page;
//    }


    //    QueryBuilders对象是用于创建查询方法的，支持多种查询类型，常用的查询API包括以下方法：

    /**
     * 关键字匹配查询
     *
     * @param name  字段的名称
     * @param value 查询值
     */
    public static TermQueryBuilder termQuery(String name, String value) {
        return new TermQueryBuilder(name, value);
    }

    public static TermQueryBuilder termQuery(String name, int value) {
        return new TermQueryBuilder(name, value);
    }

    public static TermQueryBuilder termQuery(String name, long value) {
        return new TermQueryBuilder(name, value);
    }

    public static TermQueryBuilder termQuery(String name, float value) {
        return new TermQueryBuilder(name, value);
    }

    public static TermQueryBuilder termQuery(String name, double value) {
        return new TermQueryBuilder(name, value);
    }

    public static TermQueryBuilder termQuery(String name, boolean value) {
        return new TermQueryBuilder(name, value);
    }

    public static TermQueryBuilder termQuery(String name, Object value) {
        return new TermQueryBuilder(name, value);
    }

    /**
     * 关键字查询，同时匹配多个关键字
     *
     * @param name   字段名称
     * @param values 查询值
     */
    public static TermsQueryBuilder termsQuery(String name, String... values) {
        return new TermsQueryBuilder(name, values);
    }

    /**
     * 创建一个匹配多个关键字的查询，返回boolean
     *
     * @param fieldNames 字段名称
     * @param text       查询值
     */
    public static MultiMatchQueryBuilder multiMatchQuery(Object text, String... fieldNames) {
        return new MultiMatchQueryBuilder(text, fieldNames); // BOOLEAN is the default
    }

    /**
     * 关键字，精确匹配
     *
     * @param name 字段名称
     * @param text 查询值
     */
    public static MatchQueryBuilder matchQuery(String name, Object text) {
        return new MatchQueryBuilder(name, text);
    }

    /**
     * 关键字范围查询（后面跟范围条件）
     *
     * @param name 字段名称
     */
    public static RangeQueryBuilder rangeQuery(String name) {
        return new RangeQueryBuilder(name);
    }

    /**
     * 判断字段是否有值
     *
     * @param name 字段名称
     */
    public static ExistsQueryBuilder existsQuery(String name) {
        return new ExistsQueryBuilder(name);
    }

    /**
     * 模糊查询
     *
     * @param name  字段名称
     * @param value 查询值
     */
    public static FuzzyQueryBuilder fuzzyQuery(String name, String value) {
        return new FuzzyQueryBuilder(name, value);
    }

    /**
     * 组合查询对象，可以同时引用上面的所有查询对象
     */
    public static BoolQueryBuilder boolQuery() {
        return new BoolQueryBuilder();
    }


//    聚合查询　　AggregationBuilders对象是用于创建聚合方法的，支持多种查询类型，常用的查询API包括以下方法：

    /**
     * 根据字段聚合，统计该字段的每个值的数量
     */
    public static TermsAggregationBuilder terms(String name) {
        return new TermsAggregationBuilder(name, null);
    }

    /**
     * 统计操作的，过滤条件
     */
    public static FilterAggregationBuilder filter(String name, QueryBuilder filter) {
        return new FilterAggregationBuilder(name, filter);
    }

    /**
     * 设置多个过滤条件
     */
    public static FiltersAggregationBuilder filters(String name, FiltersAggregator.KeyedFilter... filters) {
        return new FiltersAggregationBuilder(name, filters);
    }

    /**
     * 统计该字段的数据总数
     */
    public static ValueCountAggregationBuilder count(String name) {
        return new ValueCountAggregationBuilder(name, null);
    }

    /**
     * 计算平均值
     */
    public static AvgAggregationBuilder avg(String name) {
        return new AvgAggregationBuilder(name);
    }

    /**
     * 计算最大值
     */
    public static MaxAggregationBuilder max(String name) {
        return new MaxAggregationBuilder(name);
    }

    /**
     * 计算最小值
     */
    public static MinAggregationBuilder min(String name) {
        return new MinAggregationBuilder(name);
    }

    /**
     * 计算总数
     */
    public static SumAggregationBuilder sum(String name) {
        return new SumAggregationBuilder(name);
    }

}

package com.chryl;

import com.chryl.service.ChrCompanyEmpService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class ChrEsDemoApplicationTests {

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    private ChrCompanyEmpService chrCompanyEmpService;

    @Test
    public void contextLoads() {
        IndexQuery indexQuery = new IndexQuery();
        indexQuery.setIndexName("chr_company_emp");
        String index = elasticsearchTemplate.index(indexQuery);

        log.info(index);

    }

    @Test
    public void contextLoads1() {
        log.info("条数:{}", chrCompanyEmpService.count());

    }

}

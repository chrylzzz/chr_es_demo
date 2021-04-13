package com.chryl.repository;

import com.chryl.entity.ChrCompany;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * es
 * Created by Chr.yl on 2021/4/8.
 *
 * @author Chr.yl
 */
public interface ChrCompanyRepository extends ElasticsearchRepository<ChrCompany, Long> {

}

package com.chryl.repository;

import com.chryl.entity.ChrCompanyEmp;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * es
 * Created by Chr.yl on 2021/4/8.
 *
 * @author Chr.yl
 */
public interface ChrCompanyEmpRepository extends ElasticsearchRepository<ChrCompanyEmp, Long> {

    /**
     * 精确查询(包括数字):
     * 公司编号,公司名和公司简介查询
     *
     * @param companyName
     * @param companyDescription
     * @param page
     * @return
     */
    Page<ChrCompanyEmp> findByCompanyCodeOrCompanyNameOrCompanyDescription(Integer companyCode, String companyName, String companyDescription, Pageable page);


    /**
     * 模糊like 查询,不包括数字
     *
     * @param companyName
     * @param companyDescription
     * @param page
     * @return
     */
    Page<ChrCompanyEmp> findByCompanyNameLikeOrCompanyDescriptionLike(String companyName, String companyDescription, Pageable page);


    /**
     * 不包括数字
     * 包含查询 contains ,只针对英文,中文不模糊查询
     *
     * @param companyName
     * @param companyDescription
     * @param page
     * @return
     */
    Page<ChrCompanyEmp> findByCompanyNameContainsOrCompanyDescriptionContains(String companyName, String companyDescription, Pageable page);


}

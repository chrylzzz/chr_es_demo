package com.chryl.repository;

import com.chryl.entity.ChrEmp;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.math.BigDecimal;

/**
 * es
 * Created by Chr.yl on 2021/4/8.
 *
 * @author Chr.yl
 */
public interface ChrEmpRepository extends ElasticsearchRepository<ChrEmp, Long> {


    /**
     * like 查询 long和String类型,报错
     *
     * @param empName
     * @param empIdcard
     * @param empCode
     * @param page
     * @return
     */
    Page<ChrEmp> findByEmpNameLikeOrEmpIdcardLikeOrEmpCodeLike(String empName, Long empIdcard, Integer empCode, Pageable page);

    //报错
    Page<ChrEmp> findByEmpIdcardLikeOrEmpCodeLike(Long empIdcard, Integer empCode, Pageable page);

    //精确查询,可行
    Page<ChrEmp> findByEmpIdcardOrEmpCode(Long empIdcard, Integer empCode, Pageable page);

    /**
     * 精确查询数值型,可行
     *
     * @param empid
     * @param empIdcard
     * @param companyId
     * @param empsal
     * @param empCode
     * @param page
     * @return
     */
    Page<ChrEmp> findByEmpIdOrEmpIdcardOrCompanyIdOrEmpSalOrEmpCode(Long empid, Long empIdcard, Long companyId, BigDecimal empsal, Integer empCode, Pageable page);

    /**
     * 报错:
     * data es会把 string类型的数值转为long类型,导入都没法导入,难道是根据变量名称有date???
     *
     * @param strDate
     * @param page
     * @return
     */
    Page<ChrEmp> findByEmpStrDateLike(String strDate, Pageable page);

    /**
     * 数值contains查询,不报错,查不出结果
     *
     * @param strDate
     * @param page
     * @return
     */
    Page<ChrEmp> findByEmpStrDateContains(String strDate, Pageable page);
}

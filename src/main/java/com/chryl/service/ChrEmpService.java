package com.chryl.service;

import com.chryl.entity.ChrCompanyEmp;
import com.chryl.entity.ChrEmp;
import com.chryl.mapper.ChrEmpMapper;
import com.chryl.repository.ChrEmpRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Chr.yl on 2021/4/12.
 *
 * @author Chr.yl
 */
@Service
public class ChrEmpService {
    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;
    @Autowired
    private ChrEmpMapper chrEmpMapper;

    @Autowired
    private ChrEmpRepository chrEmpRepository;


    public List<ChrEmp> selectEmpAll() {
        return chrEmpMapper.selectEmpAll();
    }

    //导入全部
    public int importEmpData() {
        List<ChrEmp> chrEmpList = chrEmpMapper.selectEmpAll();
        Iterable<ChrEmp> chrCompanyEmpIterable = chrEmpRepository.saveAll(chrEmpList);
        Iterator<ChrEmp> iterator = chrCompanyEmpIterable.iterator();
        int result = 1;
        while (iterator.hasNext()) {
            result++;
            iterator.next();
        }
        return result;
    }


    public Page<ChrEmp> findByEmpNameLikeOrEmpIdcardLikeOrEmpCodeLike(String empName, Long empIdcard, Integer empCode, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        return chrEmpRepository.findByEmpNameLikeOrEmpIdcardLikeOrEmpCodeLike(empName, empIdcard, empCode, pageable);
    }

    public Page<ChrEmp> findByEmpIdcardLikeOrEmpCodeLike(Long empIdcard, Integer empCode, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        return chrEmpRepository.findByEmpIdcardLikeOrEmpCodeLike(empIdcard, empCode, pageable);
    }

    public Page<ChrEmp> findByEmpIdcardOrEmpCode(Long empIdcard, Integer empCode, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        return chrEmpRepository.findByEmpIdcardOrEmpCode(empIdcard, empCode, pageable);
    }

    public Page<ChrEmp> findByEmpIdOrEmpIdcardOrCompanyIdOrEmpSalOrEmpCode(Long empid, Long empIdcard, Long companyId, BigDecimal empsal, Integer empCode, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        return chrEmpRepository.findByEmpIdOrEmpIdcardOrCompanyIdOrEmpSalOrEmpCode(empid, empIdcard, companyId, empsal, empCode, pageable);
    }

    public Page<ChrEmp> findByEmpStrDateLike(String strDate, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        return chrEmpRepository.findByEmpStrDateLike(strDate, pageable);
    }

    public Page<ChrEmp> findByEmpStrDateContains(String strDate, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        return chrEmpRepository.findByEmpStrDateContains(strDate, pageable);
    }

}

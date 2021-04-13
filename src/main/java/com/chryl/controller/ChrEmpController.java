package com.chryl.controller;

import com.chryl.common.CommonPage;
import com.chryl.common.CommonResult;
import com.chryl.entity.ChrEmp;
import com.chryl.service.ChrEmpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 * Created by Chr.yl on 2021/4/12.
 *
 * @author Chr.yl
 */
@RestController
@RequestMapping("emp")
public class ChrEmpController {

    @Autowired
    private ChrEmpService chrEmpService;

    @GetMapping("selectEmpAll")
    public Object selectEmpAll() {
        return CommonResult.success(CommonPage.restPage(chrEmpService.selectEmpAll()));
    }

    @GetMapping("importData")
    public Object importData() {
        return chrEmpService.importEmpData();
    }

    //模糊查询long类型和int类型,报错
    @GetMapping("searchA")
    public Object findByEmpNameLikeOrEmpIdcardLikeOrEmpCodeLike(@RequestParam(required = false) String empName,
                                                                @RequestParam(required = false) Long idcard,
                                                                @RequestParam(required = false) Integer empCode,
                                                                @RequestParam(required = false, defaultValue = "0") Integer page,
                                                                @RequestParam(required = false, defaultValue = "5") Integer size) {
        Page<ChrEmp> chrEmpPage = chrEmpService.findByEmpNameLikeOrEmpIdcardLikeOrEmpCodeLike(empName, idcard, empCode, page, size);
        return CommonResult.success(CommonPage.restPage(chrEmpPage));
    }

    //模糊查询long类型和int类型,报错
    @GetMapping("searchB")
    public Object findByEmpIdcardLikeOrEmpCodeLike(
            @RequestParam(required = false) Long idcard,
            @RequestParam(required = false) Integer empCode,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "5") Integer size) {
        Page<ChrEmp> chrEmpPage = chrEmpService.findByEmpIdcardLikeOrEmpCodeLike(idcard, empCode, page, size);
        return CommonResult.success(CommonPage.restPage(chrEmpPage));
    }

    //精确查询long类型和int类型,可行
    @GetMapping("searchB1")
    public Object findByEmpIdcardOrEmpCode(
            @RequestParam(required = false) Long idcard,
            @RequestParam(required = false) Integer empCode,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "5") Integer size) {
        Page<ChrEmp> chrEmpPage = chrEmpService.findByEmpIdcardOrEmpCode(idcard, empCode, page, size);
        return CommonResult.success(CommonPage.restPage(chrEmpPage));
    }

    //精确查询 数值型,可行
    @GetMapping("searchB2")
    public Object findByEmpIdOrEmpIdcardOrCompanyIdOrEmpSalOrEmpCode(
            @RequestParam(required = false) Long empId,
            @RequestParam(required = false) Long idcard,
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) BigDecimal empSal,
            @RequestParam(required = false) Integer empCode,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "5") Integer size) {
        Page<ChrEmp> chrEmpPage = chrEmpService.findByEmpIdOrEmpIdcardOrCompanyIdOrEmpSalOrEmpCode(empId, idcard, companyId, empSal, empCode, page, size);
        return CommonResult.success(CommonPage.restPage(chrEmpPage));
    }

    //like 查询,报错
    @GetMapping("searchB3")
    public Object findByEmpStrDateLike(
            @RequestParam(required = false) String strDate,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "5") Integer size) {
        Page<ChrEmp> chrEmpPage = chrEmpService.findByEmpStrDateLike(strDate, page, size);
        return CommonResult.success(CommonPage.restPage(chrEmpPage));
    }

    //contains 查询,不报错,查不出结果
    @GetMapping("searchB4")
    public Object findByEmpStrDateContains(
            @RequestParam(required = false) String strDate,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "5") Integer size) {
        Page<ChrEmp> chrEmpPage = chrEmpService.findByEmpStrDateContains(strDate, page, size);
        return CommonResult.success(CommonPage.restPage(chrEmpPage));
    }


}

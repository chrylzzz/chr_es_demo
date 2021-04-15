package com.chryl.controller;

import com.chryl.common.CommonPage;
import com.chryl.common.CommonResult;
import com.chryl.entity.ChrCompanyEmp;
import com.chryl.entity.ChrEmp;
import com.chryl.service.ChrCompanyEmpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chr.yl on 2021/4/8.
 *
 * @author Chr.yl
 */
@RestController
@Slf4j
public class ChrCompanyEmpController {

    @Autowired
    private ChrCompanyEmpService chrCompanyEmpService;

    //获取所有
    @GetMapping("/getCompanyEmpAll")
    public Object getCompanyEmpAll() {
        return chrCompanyEmpService.getCompanyEmpAll();
    }

    //导入es
    @GetMapping("/importCompanyEmpAll")
    public Object importCompanyEmpAll() {
        return chrCompanyEmpService.importCompanyEmpAll();
    }


    @GetMapping("/indexExists/{indexName}")
    public Object indexExists(@PathVariable(required = false) String indexName) {
        return chrCompanyEmpService.indexExists(indexName);
    }

    //查询
    @GetMapping("/searchA")
    public CommonResult<CommonPage<ChrCompanyEmp>> search(@RequestParam(required = false) String keyword,
                                                          @RequestParam(required = false) Long companyId,
                                                          @RequestParam(required = false) Long empId,//empId为嵌套属性内的属性,暂时不知道如何查询
                                                          @RequestParam(required = false, defaultValue = "0") Integer page,
                                                          @RequestParam(required = false, defaultValue = "5") Integer size,
                                                          @RequestParam(required = false, defaultValue = "0") Integer sort) {
        Page<ChrCompanyEmp> chrCompanyEmpPage = chrCompanyEmpService.search(keyword, companyId, empId, page, size, sort);
        return CommonResult.success(CommonPage.restPage(chrCompanyEmpPage));
    }

    //es data 精确查询
    @GetMapping("/searchB")
    public CommonResult<CommonPage<ChrCompanyEmp>> findByCompanyNameOrCompanyDescription(
            @RequestParam(required = false) Integer companyCode, @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "5") Integer size) {
        Page<ChrCompanyEmp> chrCompanyEmpPage = chrCompanyEmpService.findByCompanyCodeOrCompanyNameOrCompanyDescription(companyCode, keyword, keyword, page, size);
        return CommonResult.success(CommonPage.restPage(chrCompanyEmpPage));
    }

    //es data 模糊查询 like 查询 ,问题:如果like查询中包含long类型和int类型,报错,未解决
    @GetMapping("/searchC")
    public CommonResult<CommonPage<ChrCompanyEmp>> findByCompanyNameLikeOrCompanyDescriptionLike(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "5") Integer size) {
        Page<ChrCompanyEmp> chrCompanyEmpPage = chrCompanyEmpService.findByCompanyNameLikeOrCompanyDescriptionLike(keyword, keyword, page, size);
        return CommonResult.success(CommonPage.restPage(chrCompanyEmpPage));
    }

    //es data 包含查询 contains 查询
    @GetMapping("/searchD")
    public CommonResult<CommonPage<ChrCompanyEmp>> findByCompanyNameContainsOrCompanyDescriptionContains(@RequestParam(required = false) String keyword,
                                                                                                         @RequestParam(required = false, defaultValue = "0") Integer page,
                                                                                                         @RequestParam(required = false, defaultValue = "5") Integer size) {
        Page<ChrCompanyEmp> chrCompanyEmpPage = chrCompanyEmpService.findByCompanyNameContainsOrCompanyDescriptionContains(keyword, keyword, page, size);
        return CommonResult.success(CommonPage.restPage(chrCompanyEmpPage));
    }

    //searchQuery 各种查询
    @GetMapping("/searchE")
    public CommonResult<CommonPage<ChrCompanyEmp>> searchQuery(@RequestParam(required = false) String keyword,
                                                               @RequestParam(required = false, defaultValue = "0") Integer page,
                                                               @RequestParam(required = false, defaultValue = "5") Integer size) {
        List<ChrCompanyEmp> chrCompanyEmpPage = chrCompanyEmpService.searchQuery(keyword, page, size);
        return CommonResult.success(CommonPage.restPage(chrCompanyEmpPage));
    }


    /**
     * 中文分词复杂查询:
     * 数字查询未解决,
     * 其他多调价查询,可行,
     * 设置了中文分词才可以中文查询,但是只可以一个一个字查询
     */
    @GetMapping("/searchE1")
    public CommonResult<CommonPage<ChrCompanyEmp>> searchChinesQuery(
            @RequestParam(required = false) Integer companyCode,
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) Long empId,
            @RequestParam(required = false) Integer empCode,
            @RequestParam(required = false) String companyName,
            @RequestParam(required = false) String companyChinese,
            @RequestParam(required = false) String companyDescription,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "5") Integer size) {
        List<ChrCompanyEmp> chrCompanyEmpPage = chrCompanyEmpService.searchChinesQuery
                (companyCode, companyId, empId, empCode, companyName, companyChinese, companyDescription, page, size);
        return CommonResult.success(CommonPage.restPage(chrCompanyEmpPage));
    }


    /**
     * ES CRUD
     */
    @GetMapping("show")
    public void show() {
        log.info("索引存在 , {}", chrCompanyEmpService.indexExists());
        log.info("索引存在 , {}", chrCompanyEmpService.indexExists("chr_company_emp"));
//        log.info("删除索引 , {}", chrCompanyEmpService.indexDelete("chr_company_emp"));

//        log.info("创建索引 , {}", chrCompanyEmpService.indexCreate());
//        log.info("创建索引 , {}", chrCompanyEmpService.indexCreate("chr_company_emp"));


        ChrCompanyEmp chrCompanyEmp = new ChrCompanyEmp();
        chrCompanyEmp.setCompanyId(200199700L);
        chrCompanyEmp.setCompanyCode(8901);
        chrCompanyEmp.setCompanyChinese("nono mean");
//        chrCompanyEmp.setCompanyChinese("测试update");
        chrCompanyEmp.setCompanyName("nono mean");
        chrCompanyEmp.setCompanyDescription("nono mean");
        List<ChrEmp> list = new ArrayList<>();
        ChrEmp chrEmp = new ChrEmp();
        chrEmp.setEmpId(2389121313L);
        chrEmp.setEmpName("zhousx");
        chrEmp.setEmpRealName("周盛鑫");
        list.add(chrEmp);
        chrCompanyEmp.setChrEmpList(list);
        List<ChrCompanyEmp> companyEmpList = new ArrayList<>();
        companyEmpList.add(chrCompanyEmp);

        //逐条插入
        chrCompanyEmpService.save(chrCompanyEmp);
        //逐条删除
//        chrCompanyEmpService.deleteById(200199700L);
//        chrCompanyEmpService.deleteByBean(chrCompanyEmp);

        //批量删除
//        chrCompanyEmpService.deleteAll(companyEmpList);
//        chrCompanyEmpService.deleteAll();
        //批量插入
//        chrCompanyEmpService.saveAll(companyEmpList);
        //逐条更新
//        chrCompanyEmpService.update("chr_company_emp", "company_emp", chrCompanyEmp);

        //批量更新
        chrCompanyEmp.setCompanyChinese("测试update");
        chrCompanyEmpService.batchUpdate("chr_company_emp", "company_emp", companyEmpList);
    }


}

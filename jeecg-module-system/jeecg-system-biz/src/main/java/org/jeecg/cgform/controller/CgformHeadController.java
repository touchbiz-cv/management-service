package org.jeecg.cgform.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.cgform.database.domain.CgformHeadDO;
import org.jeecg.cgform.entity.CgTableEntity;
import org.jeecg.cgform.service.CgformHeadService;
import org.jeecg.cgform.service.impl.OnlCgformHeadServiceImpl;
import org.jeecg.cgform.utils.DbReadTableUtil;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.PermissionData;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.online.cgform.entity.OnlCgformHead;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static org.jeecg.common.constant.DataBaseConstant.ID;
import static org.jeecg.common.constant.SymbolConstant.COMMA;

/**
 * @author jiangyan
 */
@Slf4j
@RestController
@RequestMapping({"/api/online/cgform/head"})
public class CgformHeadController extends JeecgController<OnlCgformHead, OnlCgformHeadServiceImpl> {

    private final CgformHeadService cgformHeadService;

    public CgformHeadController(CgformHeadService cgformHeadService) {
        this.cgformHeadService = cgformHeadService;
    }

    @GetMapping({"/list"})
    @PermissionData
    public Result<IPage<CgformHeadDO>> list(CgformHeadDO head, @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo, @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize
            , HttpServletRequest request) {
        Result<IPage<CgformHeadDO>> result = new Result<>();
        QueryWrapper<CgformHeadDO> var6 = QueryGenerator.initQueryWrapper(head, request.getParameterMap());
        var page = new Page<CgformHeadDO>((long) pageNo, (long) pageSize);
        var pageResult = cgformHeadService.page(page, var6);
        if (head.getCopyType() != null && head.getCopyType() == 0) {
            cgformHeadService.initCopyState(pageResult.getRecords());
        }
        result.setSuccess(true);
        result.setResult(pageResult);
        return result;
    }

    @Deprecated
    @GetMapping({"/queryTables"})
    public Result<?> queryTables(@RequestParam(name = "tableName", required = false) String tableName, @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo, @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize, HttpServletRequest var4) {
        String userName = JwtUtil.getUserNameByToken(var4);
        log.info("userName:{}", userName);
        if (!userName.toLowerCase().contains("admin")) {
            return Result.error("noadminauth");
        }
        List<String> allTableNames;
        try {
            DataSource dataSource = SpringContextUtils.getBean(DataSource.class);
            allTableNames = DbReadTableUtil.readAllTableNames(dataSource.getConnection());
        } catch (SQLException var12) {
            log.error(var12.getMessage(), var12);
            return Result.error("同步失败，未获取数据库表信息");
        }

        allTableNames.sort(Comparator.comparing(String::toLowerCase));
        allTableNames = allTableNames.stream().map(String::toLowerCase)
                .collect(Collectors.toList());


        if (!ObjectUtils.isEmpty(tableName)) {
            allTableNames = allTableNames.stream().filter(x -> x.contains(tableName)).toList();
        }
        var result = allTableNames.stream().map(var10 -> {
            HashMap<String, String> var11 = new HashMap<>(1);
            var11.put(ID, var10);
            return var11;
        }).toList();

        return Result.ok(result);
    }

    /**
     * 查询的同时返回表类型
     *
     * @param tableName
     * @param pageNo
     * @param pageSize
     * @param var4
     * @return
     */
    @GetMapping({"/queryTables/v2"})
    public Result<List<CgTableEntity>> queryTablesV2(@RequestParam(name = "tableName", required = false) String tableName, @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo, @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize, HttpServletRequest var4) {
        String userName = JwtUtil.getUserNameByToken(var4);
        log.info("userName:{}", userName);
        if (!userName.toLowerCase().contains("admin")) {
            return Result.error("noadminauth");
        }
        List<CgTableEntity> allTableNames;
        try {
            DataSource dataSource = SpringContextUtils.getBean(DataSource.class);
            allTableNames = DbReadTableUtil.readAllTableNamesV2(dataSource.getConnection());
        } catch (SQLException var12) {
            log.error(var12.getMessage(), var12);
            return Result.error("同步失败，未获取数据库表信息");
        }

        allTableNames.sort(Comparator.comparing(CgTableEntity::getTableName));
        allTableNames.forEach(x -> x.setTableName(x.getTableName().toLowerCase()));


        var onlineTables = getService().queryOnlinetables();
        var result = allTableNames.stream()
                .filter(x -> ObjectUtils.isEmpty(tableName) || x.getTableName().contains(tableName))
                .filter(tbName -> !onlineTables.contains(tbName.getTableName()))
                .toList();
        return Result.ok(result);
    }

    @PostMapping({"/transTables/{tbNames}"})
    public Result<?> transTables(@PathVariable("tbNames") String tbNames, HttpServletRequest request) {
        try {
            String userName = JwtUtil.getUserNameByToken(request);
            log.info("userName:{}", userName);
            if (!userName.toLowerCase().contains("admin")) {
                return Result.error("noadminauth");
            }
            var onlineTables = getService().queryOnlinetables();
            Arrays.stream(tbNames.split(COMMA)).filter(tableName -> oConvertUtils.isNotEmpty(tableName) && !onlineTables.contains(tableName))
                    .forEach(tableName -> getService().saveDbTable2Online(tableName));
            return Result.ok("同步完成!");
        } catch (Exception err) {
            log.error("", err);
            return Result.error(err.getMessage());
        }
    }

    private boolean filterTable(List<String> filterPrefix, String var1) {
        return filterPrefix.stream().noneMatch(var3 -> var1.startsWith(var3) || var1.startsWith(var3.toUpperCase()));
    }
}

package org.jeecg.cgform.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.cgform.service.CgformFieldService;
import org.jeecg.cgform.service.CgformHeadService;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.online.auth.entity.OnlAuthData;
import org.jeecg.modules.online.auth.entity.OnlAuthPage;
import org.jeecg.modules.online.auth.entity.OnlAuthRelation;
import org.jeecg.modules.online.auth.service.IOnlAuthDataService;
import org.jeecg.modules.online.auth.service.IOnlAuthPageService;
import org.jeecg.modules.online.auth.service.IOnlAuthRelationService;
import org.jeecg.modules.online.auth.vo.AuthColumnVO;
import org.jeecg.modules.online.auth.vo.AuthPageVO;
import org.jeecg.modules.online.cgform.d.b;
import org.jeecg.modules.online.cgform.entity.OnlCgformButton;
import org.jeecg.modules.online.cgform.entity.OnlCgformField;
import org.jeecg.modules.online.cgform.service.*;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static org.jeecg.common.constant.SymbolConstant.COMMA;

/**
 * @author jiangyan
 */
@Slf4j
@RestController
@RequestMapping({"/api/online/cgform/api"})
public class CgformAuthController extends JeecgController<OnlCgformButton, IOnlCgformButtonService> {

    private final CgformFieldService cgformFieldService;
    private final IOnlAuthDataService onlAuthDataService;

    private final IOnlAuthPageService onlAuthPageService;
    private final IOnlAuthRelationService onlAuthRelationService;

    private final CgformHeadService cgformHeadService;

    public CgformAuthController(CgformFieldService cgformFieldService, IOnlAuthDataService onlAuthDataService, IOnlAuthPageService onlAuthPageService, IOnlAuthRelationService onlAuthRelationService, CgformHeadService cgformHeadService) {
        this.cgformFieldService = cgformFieldService;
        this.onlAuthDataService = onlAuthDataService;
        this.onlAuthPageService = onlAuthPageService;
        this.onlAuthRelationService = onlAuthRelationService;
        this.cgformHeadService = cgformHeadService;
    }


    @GetMapping({"/authData/{cgformId}"})
    public Result<List<OnlAuthData>> authData(@PathVariable("cgformId") String cgformId) {
        var result = new Result<List<OnlAuthData>>();
        LambdaQueryWrapper<OnlAuthData> var3 = new LambdaQueryWrapper<>();
        var3.eq(OnlAuthData::getCgformId, cgformId);
        var var4 = this.onlAuthDataService.list(var3);
        result.setResult(var4);
        result.setSuccess(true);
        return result;
    }

    @PostMapping({"/authData"})
    @CacheEvict(
            value = {"sys:cache:online:list", "sys:cache:online:form"},
            allEntries = true,
            beforeInvocation = true
    )
    public Result<OnlAuthData> authData(@RequestBody OnlAuthData onlAuthData) {
        var result = new Result<OnlAuthData>();

        try {
            this.onlAuthDataService.save(onlAuthData);
            result.success("添加成功！");
        } catch (Exception var4) {
            log.error(var4.getMessage(), var4);
            result.error500("操作失败");
        }
        return result;
    }

    @PutMapping({"/authData"})
    @CacheEvict(
            value = {"sys:cache:online:list", "sys:cache:online:form"},
            allEntries = true,
            beforeInvocation = true
    )
    public Result<OnlAuthData> authDataUpdate(@RequestBody OnlAuthData onlAuthData) {
        var result = new Result<OnlAuthData>();
        this.onlAuthDataService.updateById(onlAuthData);
        result.success("编辑成功！");
        return result;
    }

    @DeleteMapping({"/authData/{id}"})
    @CacheEvict(
            value = {"sys:cache:online:list", "sys:cache:online:form"},
            allEntries = true,
            beforeInvocation = true
    )
    public Result<?> authDataById(@PathVariable("id") String id) {
        this.onlAuthDataService.deleteOne(id);
        return Result.ok("删除成功!");
    }

    @PostMapping({"/createAiTestAuthData"})
    @CacheEvict(
            value = {"sys:cache:online:list", "sys:cache:online:form"},
            allEntries = true,
            beforeInvocation = true
    )
    public Result<?> createAiTestAuthData(@RequestBody JSONObject jsonObject) {
        Result<?> result = new Result<>();

        try {
            this.onlAuthDataService.createAiTestAuthData(jsonObject);
            result.success("添加成功！");
        } catch (Exception var4) {
            log.error(var4.getMessage(), var4);
            result.error500("操作失败");
        }

        return result;
    }

    @GetMapping({"/authButton/{cgformId}"})
    public Result<Map<String, Object>> authButtonByCgFormId(@PathVariable("cgformId") String cgformId) {
        var result = new Result<Map<String, Object>>();
        var queryWrapper = new LambdaQueryWrapper<OnlCgformButton>()
                    .eq(OnlCgformButton::getCgformHeadId, cgformId)
                .eq(OnlCgformButton::getButtonStatus, "1")
                .select(OnlCgformButton::getButtonCode, OnlCgformButton::getButtonName, OnlCgformButton::getButtonStyle);
        var cgformButtonList = this.getService().list(queryWrapper);
        var lambdaQueryWrapper = new LambdaQueryWrapper<OnlAuthPage>().eq(OnlAuthPage::getCgformId, cgformId).eq(OnlAuthPage::getType, 2);
        var onlAuthPages = this.onlAuthPageService.list(lambdaQueryWrapper);
        Map<String, Object> hashMap = new HashMap<>(5);
        hashMap.put("buttonList", cgformButtonList);
        hashMap.put("authList", onlAuthPages);
        result.setResult(hashMap);
        result.setSuccess(true);
        return result;
    }

    @PostMapping({"/authButton"})
    @CacheEvict(
            value = {"sys:cache:online:list", "sys:cache:online:form"},
            allEntries = true,
            beforeInvocation = true
    )
    public Result<OnlAuthPage> authButton(@RequestBody OnlAuthPage request) {
        var result = new Result<OnlAuthPage>();

        try {
            boolean exist = false;
            if (oConvertUtils.isNotEmpty(request.getId())) {
                OnlAuthPage authPage = this.onlAuthPageService.getById(request.getId());
                if (authPage != null) {
                    exist = true;
                    authPage.setStatus(1);
                    this.onlAuthPageService.updateById(authPage);
                }
            }

            if (!exist) {
                request.setStatus(1);
                this.onlAuthPageService.save(request);
            }

            result.setResult(request);
            result.success("操作成功！");
        } catch (Exception var6) {
            log.error(var6.getMessage(), var6);
            result.error500("操作失败");
        }

        return result;
    }

    @PutMapping({"/authButton/{id}"})
    @CacheEvict(
            value = {"sys:cache:online:list", "sys:cache:online:form"},
            allEntries = true,
            beforeInvocation = true
    )
    public Result<?> authButton(@PathVariable("id") String id) {
        var updateWrapper = new UpdateWrapper<OnlAuthPage>().lambda().eq(OnlAuthPage::getId, id)
                .set(OnlAuthPage::getStatus, 0);
        this.onlAuthPageService.update(updateWrapper);
        return Result.ok("操作成功");
    }

    @GetMapping({"/authColumn/{cgformId}"})
    public Result<List<AuthColumnVO>> authColumn(@PathVariable("cgformId") String cgformId) {
        var result = new Result<List<AuthColumnVO>>();
        var fieldList = cgformFieldService.listFieldByHead(cgformId);
        if (CollectionUtils.isEmpty(fieldList)) {
            Result.error("未找到对应字段信息!");
        }

        var queryWrapper = new LambdaQueryWrapper<OnlAuthPage>().eq(OnlAuthPage::getCgformId, cgformId)
                .eq(OnlAuthPage::getType, 1);
        var authPageList = this.onlAuthPageService.list(queryWrapper);
        List<AuthColumnVO> authColumnList = new ArrayList<>();

        for (OnlCgformField cgformField : fieldList) {
            var column = new AuthColumnVO(cgformField);
            Integer status = 0;
            boolean listShow = false;
            boolean formShow = false;
            boolean formEditable = false;

            for (OnlAuthPage onlAuthPage : authPageList) {
                if (cgformField.getDbFieldName().equals(onlAuthPage.getCode())) {
                    status = onlAuthPage.getStatus();
                    if (onlAuthPage.getPage() == 3 && onlAuthPage.getControl() == 5) {
                        listShow = true;
                    }

                    if (onlAuthPage.getPage() == 5) {
                        if (onlAuthPage.getControl() == 5) {
                            formShow = true;
                        } else if (onlAuthPage.getControl() == 3) {
                            formEditable = true;
                        }
                    }
                }
            }
            column.setStatus(status);
            column.setListShow(listShow);
            column.setFormShow(formShow);
            column.setFormEditable(formEditable);
            authColumnList.add(column);
        }

        result.setResult(authColumnList);
        return result;
    }

    @PutMapping({"/authColumn"})
    @CacheEvict(
            value = {"sys:cache:online:list", "sys:cache:online:form"},
            allEntries = true,
            beforeInvocation = true
    )
    public Result<?> authColumnUpdate(@RequestBody AuthColumnVO column) {
        Result<?> result = new Result<>();

        try {
            if (column.getStatus() == 1) {
                this.onlAuthPageService.enableAuthColumn(column);
            } else {
                this.onlAuthPageService.disableAuthColumn(column);
            }
            result.success("操作成功！");
        } catch (Exception var4) {
            log.error(var4.getMessage(), var4);
            result.error500("操作失败");
        }

        return result;
    }

    @PostMapping({"/authColumn"})
    @CacheEvict(
            value = {"sys:cache:online:list", "sys:cache:online:form"},
            allEntries = true,
            beforeInvocation = true
    )
    public Result<?> authColumn(@RequestBody AuthColumnVO columnVO) {
        Result<?> result = new Result<>();

        try {
            this.onlAuthPageService.switchAuthColumn(columnVO);
            result.success("操作成功！");
        } catch (Exception var4) {
            log.error(var4.getMessage(), var4);
            result.error500("操作失败");
        }

        return result;
    }

    @GetMapping({"/authPage/{cgformId}/{type}"})
    public Result<List<AuthPageVO>> authPage(@PathVariable("cgformId") String cgformId, @PathVariable("type") Integer var2) {
        var authPageList = this.onlAuthPageService.queryAuthByFormId(cgformId, var2);
        return Result.ok(authPageList);
    }

    @GetMapping({"/validAuthData/{cgformId}"})
    public Result<List<OnlAuthData>> validAuthData(@PathVariable("cgformId") String cgformId) {
        LambdaQueryWrapper<OnlAuthData> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OnlAuthData::getCgformId, cgformId)
                .eq(OnlAuthData::getStatus, 1)
                .select(OnlAuthData::getId, OnlAuthData::getRuleName);
        var list = this.onlAuthDataService.list(queryWrapper);
        return Result.ok(list);
    }

    @GetMapping({"/roleAuth"})
    public Result<List<OnlAuthRelation>> roleAuth(@RequestParam("roleId") String roleId, @RequestParam("cgformId") String cgformId, @RequestParam("type") Integer type,
                                                  @RequestParam("authMode") String authMode) {
        var queryWrapper = new LambdaQueryWrapper<OnlAuthRelation>();
        queryWrapper.eq(OnlAuthRelation::getRoleId, roleId)
                .eq(OnlAuthRelation::getCgformId, cgformId)
                .eq(OnlAuthRelation::getType, type)
                .eq(OnlAuthRelation::getAuthMode, authMode)
                .select(OnlAuthRelation::getAuthId);
        var onlAuthRelations = this.onlAuthRelationService.list(queryWrapper);
        return Result.ok(onlAuthRelations);
    }

    @PostMapping({"/roleColumnAuth/{roleId}/{cgformId}"})
    @CacheEvict(
            value = {"sys:cache:online:list", "sys:cache:online:form"},
            allEntries = true,
            beforeInvocation = true
    )
    public Result<?> roleColumnAuth(@PathVariable("roleId") String roleId, @PathVariable("cgformId") String cgformId,
                                    @RequestBody JSONObject jsonObject) {
        JSONArray authId = jsonObject.getJSONArray("authId");
        String authMode = jsonObject.getString("authMode");
        var javaList = authId.toJavaList(String.class);
        this.onlAuthRelationService.saveRoleAuth(roleId, cgformId, 1, authMode, javaList);
        return Result.ok();
    }

    @PostMapping({"/roleButtonAuth/{roleId}/{cgformId}"})
    @CacheEvict(
            value = {"sys:cache:online:list", "sys:cache:online:form"},
            allEntries = true,
            beforeInvocation = true
    )
    public Result<?> roleButtonAuth(@PathVariable("roleId") String roleId, @PathVariable("cgformId") String cgformId, @RequestBody JSONObject jsonObject) {
        JSONArray authId = jsonObject.getJSONArray("authId");
        String authMode = jsonObject.getString("authMode");
        var javaList = authId.toJavaList(String.class);
        this.onlAuthRelationService.saveRoleAuth(roleId, cgformId, 2, authMode, javaList);
        return Result.ok();
    }

    @PostMapping({"/roleDataAuth/{roleId}/{cgformId}"})
    @CacheEvict(
            value = {"sys:cache:online:list", "sys:cache:online:form"},
            allEntries = true,
            beforeInvocation = true
    )
    public Result<?> roleDataAuth(@PathVariable("roleId") String roleId, @PathVariable("cgformId") String cgformId, @RequestBody JSONObject jsonObject) {
        JSONArray authId = jsonObject.getJSONArray("authId");
        String authMode = jsonObject.getString("authMode");
        var javaList = authId.toJavaList(String.class);
        this.onlAuthRelationService.saveRoleAuth(roleId, cgformId, 3, authMode, javaList);
        return Result.ok();
    }

    @GetMapping({"/getAuthColumn/{desformCode}"})
    public Result<List<AuthColumnVO>> getAuthColumn(@PathVariable("desformCode") String desformCode) {
        var head =  cgformHeadService.getByTableName(desformCode);
        if (head == null) {
            Result.error("未找到对应字段信息!");
        }

        var fieldList = cgformFieldService.listFieldByHead(head.getId());
        if (CollectionUtils.isEmpty(fieldList)) {
            Result.error("未找到对应字段信息!");
        }


        List<AuthColumnVO> var6 = new ArrayList<>();

        for (var fieldDO : fieldList) {
            if (!b.i(fieldDO.getDbFieldName())) {
                AuthColumnVO var9 = new AuthColumnVO(fieldDO);
                var9.setTableName(head.getTableName());
                var9.setTableNameTxt(head.getTableTxt());
                var9.setIsMain(true);
                var6.add(var9);
            }
        }

        if (oConvertUtils.isNotEmpty(head.getSubTableStr())) {
            String[] var17 = head.getSubTableStr().split(COMMA);

            for (String subTableName : var17) {
                var subTableHead = cgformHeadService.getByTableName(subTableName);
                if (subTableHead != null) {
                    var subTableFieldList = cgformFieldService.listFieldByHead(subTableHead.getId());
                    if (subTableFieldList != null) {

                        for (var fieldDO : subTableFieldList) {
                            if (!b.i(fieldDO.getDbFieldName())) {
                                AuthColumnVO columnVO = new AuthColumnVO(fieldDO);
                                columnVO.setTableName(subTableHead.getTableName());
                                columnVO.setTableNameTxt(subTableHead.getTableTxt());
                                columnVO.setIsMain(false);
                                var6.add(columnVO);
                            }
                        }
                    }
                }
            }
        }
        return Result.ok(var6);
    }
}

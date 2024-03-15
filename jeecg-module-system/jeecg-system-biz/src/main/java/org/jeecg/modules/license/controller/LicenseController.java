package org.jeecg.modules.license.controller;

import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.license.application.LicenseApplication;
import org.jeecg.modules.license.dto.LicenseActivationRequest;
import org.jeecg.modules.system.service.ISysUserService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author jiangyan
 */
@Slf4j
@RestController
@RequestMapping("/api/system/license")
public class LicenseController {

    private final LicenseApplication licenseApplication;

    private final ISysUserService sysUserService;

    public LicenseController(LicenseApplication licenseApplication, ISysUserService sysUserService) {
        this.licenseApplication = licenseApplication;
        this.sysUserService = sysUserService;
    }

    @ApiOperation(value = "查询license以及激活状态的接口")
    @GetMapping("status")
    public Result<?> status() {
        try {
            var response = licenseApplication.getStatus();
            if (response != null && response.getInfo() != null) {
                var license = response.getInfo();
                if (license.getLicenseExtraModel() != null) {
//                    license.getLicenseExtraModel().setUesdModuleAmount(moduleService.count());//TODO 暂时注释
                    license.getLicenseExtraModel().setUsedUserAmount(sysUserService.count());
                }
            }
            return Result.OK(response);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Result.error(500, "查询失败");
        }
    }

    @PostMapping("activation")
    public Result<?> active
            (@RequestBody @Validated LicenseActivationRequest request) {
        try {
            licenseApplication.activation(request);
            return Result.OK("激活成功！");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Result.error(500, e.getMessage());
        }
    }

    @GetMapping("volume")
    public Result<?> volume() {
        try {
            return Result.OK("查询成功！");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Result.error(500, "操作失败");
        }
    }
}

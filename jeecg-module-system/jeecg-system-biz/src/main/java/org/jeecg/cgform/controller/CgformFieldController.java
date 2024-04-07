package org.jeecg.cgform.controller;

import org.jeecg.cgform.service.CgformFieldService;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.cgform.database.domain.CgformFieldDO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author jiangyan
 */
@RestController
@RequestMapping({"/api/online/cgform/field"})
public class CgformFieldController extends JeecgController<CgformFieldDO, CgformFieldService> {

    @GetMapping({"/listByHeadId"})
    public Result<List<CgformFieldDO>> listByHeadId(@RequestParam("headId") String headId) {
        return Result.ok(getService().listFieldByHead(headId));
    }
}

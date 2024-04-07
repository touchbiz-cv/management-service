package org.jeecg.modules.base.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * @author jiangyan
 */
public abstract class BizServiceImpl<M extends BaseMapper<T>, T> extends ServiceImpl<M, T> {

    public LambdaQueryWrapper<T> createBizQueryWrapper() {
        return new LambdaQueryWrapper<>();
    }

}

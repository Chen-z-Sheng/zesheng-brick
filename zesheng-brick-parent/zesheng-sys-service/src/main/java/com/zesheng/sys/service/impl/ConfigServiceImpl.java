package com.zesheng.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zesheng.common.enums.ResultCodeEnum;
import com.zesheng.common.util.ConfigValueParser;
import com.zesheng.common.request.PageAndSortQueryRequest;
import com.zesheng.common.response.R;
import com.zesheng.sys.entity.ConfigEntity;
import com.zesheng.sys.mapper.ConfigMapper;
import com.zesheng.sys.model.request.ConfigSaveRequest;
import com.zesheng.sys.model.request.ConfigUpdateRequest;
import com.zesheng.sys.service.IConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.List;

@Slf4j
@Service
public class ConfigServiceImpl implements IConfigService {

    @Resource
    private ConfigMapper configMapper;

    @Override
    @Transactional(readOnly = true)
    public IPage<ConfigEntity> pageConfig(PageAndSortQueryRequest queryDto) {
        Page<ConfigEntity> page = new Page<>(queryDto.getPageNum(), queryDto.getPageSize());
        LambdaQueryWrapper<ConfigEntity> queryWrapper = new LambdaQueryWrapper<>();
        return configMapper.selectPage(page, queryWrapper);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConfigEntity> list() {
        return configMapper.selectList(new LambdaQueryWrapper<ConfigEntity>().orderByAsc(ConfigEntity::getId));
    }

    @Override
    @Transactional(readOnly = true)
    public ConfigEntity getById(Long id) {
        return configMapper.selectById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public ConfigEntity getByKey(String configKey) {
        return configMapper.selectOne(
                new LambdaQueryWrapper<ConfigEntity>().eq(ConfigEntity::getConfigKey, configKey));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<ConfigEntity> save(ConfigSaveRequest request) {
        ConfigEntity existing = getByKey(request.getConfigKey());
        if (existing != null) {
            return R.error(ResultCodeEnum.CONFIG_EXIST);
        }
        ConfigEntity entity = new ConfigEntity();
        entity.setConfigKey(request.getConfigKey());
        entity.setValue(request.getValue());
        entity.setValueType(ConfigValueParser.normalizeValueType(
                request.getValueType() != null ? request.getValueType() : "json"));
        entity.setRemark(request.getRemark());
        configMapper.insert(entity);
        return R.success(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<ConfigEntity> update(Long id, ConfigUpdateRequest request) {
        ConfigEntity entity = configMapper.selectById(id);
        if (entity == null) {
            return R.error(ResultCodeEnum.CONFIG_NOT_FOUND);
        }
        if (request.getValue() != null) {
            entity.setValue(request.getValue());
        }
        if (request.getValueType() != null) {
            entity.setValueType(ConfigValueParser.normalizeValueType(request.getValueType()));
        }
        if (request.getRemark() != null) {
            entity.setRemark(request.getRemark());
        }
        configMapper.updateById(entity);
        return R.success(configMapper.selectById(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Integer> deleteById(Long id) {
        ConfigEntity entity = configMapper.selectById(id);
        if (entity == null) {
            return R.error(ResultCodeEnum.CONFIG_NOT_FOUND);
        }
        int rows = configMapper.deleteById(id);
        return R.success(rows);
    }
}

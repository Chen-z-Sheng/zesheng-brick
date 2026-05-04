package com.zesheng.client.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zesheng.client.entity.UserSender;
import com.zesheng.client.mapper.UserSenderMapper;
import com.zesheng.client.service.IUserSenderService;
import com.zesheng.common.response.R;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 用户寄件人信息 Service 实现
 */
@Service
@RequiredArgsConstructor
public class UserSenderServiceImpl implements IUserSenderService {

    private final UserSenderMapper userSenderMapper;

    @Override
    public List<UserSender> listByUserId(Long userId) {
        if (userId == null) {
            return List.of();
        }
        return userSenderMapper.listByUserIdOrderByUseCountDesc(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<UserSender> add(Long userId, String name, String phone) {
        if (userId == null) {
            return R.error("用户未登录");
        }
        if (!StringUtils.hasText(name)) {
            return R.error("请输入寄件人姓名");
        }
        if (!StringUtils.hasText(phone)) {
            return R.error("请输入手机号");
        }
        UserSender entity = new UserSender();
        entity.setUserId(userId);
        entity.setName(name.trim());
        entity.setPhone(phone.trim());
        userSenderMapper.insert(entity);
        return R.success(userSenderMapper.selectById(entity.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<UserSender> update(Long id, Long userId, String name, String phone) {
        if (userId == null) {
            return R.error("用户未登录");
        }
        UserSender entity = userSenderMapper.selectById(id);
        if (entity == null || !entity.getUserId().equals(userId)) {
            return R.error("寄件人不存在或无权修改");
        }
        if (StringUtils.hasText(name)) {
            entity.setName(name.trim());
        }
        if (StringUtils.hasText(phone)) {
            entity.setPhone(phone.trim());
        }
        userSenderMapper.updateById(entity);
        return R.success(userSenderMapper.selectById(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Integer> delete(Long id, Long userId) {
        if (userId == null) {
            return R.error("用户未登录");
        }
        UserSender entity = userSenderMapper.selectById(id);
        if (entity == null || !entity.getUserId().equals(userId)) {
            return R.error("寄件人不存在或无权删除");
        }
        int rows = userSenderMapper.deleteById(id);
        return R.success(rows);
    }
}

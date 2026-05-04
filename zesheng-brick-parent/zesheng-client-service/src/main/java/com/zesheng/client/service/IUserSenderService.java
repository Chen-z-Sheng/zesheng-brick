package com.zesheng.client.service;

import com.zesheng.client.entity.UserSender;
import com.zesheng.common.response.R;

import java.util.List;

/**
 * 用户寄件人信息 Service（仅 C 端）
 */
public interface IUserSenderService {

    List<UserSender> listByUserId(Long userId);

    R<UserSender> add(Long userId, String name, String phone);

    R<UserSender> update(Long id, Long userId, String name, String phone);

    R<Integer> delete(Long id, Long userId);
}

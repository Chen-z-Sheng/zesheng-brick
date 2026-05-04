package com.zesheng.client.service;

import com.zesheng.client.entity.HelpFaq;

import java.util.List;

/**
 * 帮助中心FAQ Service接口
 */
public interface IHelpFaqService {

    /**
     * 获取启用的FAQ列表（按sort_order升序）
     */
    List<HelpFaq> listEnabled();
}

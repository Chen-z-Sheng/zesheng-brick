package com.zesheng.client.service;

import com.zesheng.client.entity.Announcement;

/**
 * 小程序端公告 Service 接口
 */
public interface IAnnouncementService {

    /**
     * 获取当前用户需要弹窗展示的启用公告
     *
     * @param userId 当前登录用户ID（JWT）
     * @return 需弹窗的公告，无则 null
     */
    Announcement getLatestToShow(Long userId);

    /**
     * 查询公告历史列表
     *
     * @return 公告列表
     */
    java.util.List<Announcement> listHistory();

    /**
     * 用户选择“不再弹窗”，写入忽略记录
     *
     * @param userId        当前用户ID
     * @param announcementId 公告ID
     */
    void ignore(Long userId, Long announcementId);
}

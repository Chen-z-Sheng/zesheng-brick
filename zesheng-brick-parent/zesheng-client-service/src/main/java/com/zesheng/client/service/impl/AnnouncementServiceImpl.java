package com.zesheng.client.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zesheng.client.entity.Announcement;
import com.zesheng.client.entity.AnnouncementIgnore;
import com.zesheng.client.entity.User;
import com.zesheng.client.mapper.AnnouncementIgnoreMapper;
import com.zesheng.client.mapper.AnnouncementMapper;
import com.zesheng.client.mapper.UserMapper;
import com.zesheng.client.service.IAnnouncementService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 小程序端公告 Service 实现
 */
@Slf4j
@Service
public class AnnouncementServiceImpl implements IAnnouncementService {

    @Resource
    private AnnouncementMapper announcementMapper;
    @Resource
    private AnnouncementIgnoreMapper announcementIgnoreMapper;
    @Resource
    private UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public Announcement getLatestToShow(Long userId) {
        if (userId == null) {
            return null;
        }
        User user = userMapper.selectById(userId);
        if (user == null || user.getOpenid() == null || user.getOpenid().isBlank()) {
            return null;
        }
        String openid = user.getOpenid();
        // 查询当前启用的公告，按更新时间倒序取一条
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Announcement::getStatus, 1)
                .orderByDesc(Announcement::getUpdatedAt)
                .orderByDesc(Announcement::getId)
                .last("LIMIT 1");
        List<Announcement> list = announcementMapper.selectList(wrapper);
        if (list.isEmpty()) {
            return null;
        }
        Announcement latest = list.get(0);

        // 检查该用户是否已忽略此公告
        long count = announcementIgnoreMapper.selectCount(
                new LambdaQueryWrapper<AnnouncementIgnore>()
                        .eq(AnnouncementIgnore::getAnnouncementId, latest.getId())
                        .eq(AnnouncementIgnore::getOpenid, openid));
        if (count > 0) {
            return null;
        }
        return latest;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Announcement> listHistory() {
        return announcementMapper.selectList(
                new LambdaQueryWrapper<Announcement>()
                        .orderByDesc(Announcement::getStatus)
                        .orderByDesc(Announcement::getUpdatedAt)
                        .orderByDesc(Announcement::getId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void ignore(Long userId, Long announcementId) {
        if (userId == null || announcementId == null) {
            return;
        }
        User user = userMapper.selectById(userId);
        if (user == null || user.getOpenid() == null || user.getOpenid().isBlank()) {
            return;
        }
        String openid = user.getOpenid();
        long existed = announcementIgnoreMapper.selectCount(
                new LambdaQueryWrapper<AnnouncementIgnore>()
                        .eq(AnnouncementIgnore::getAnnouncementId, announcementId)
                        .eq(AnnouncementIgnore::getOpenid, openid));
        if (existed > 0) {
            return;
        }
        AnnouncementIgnore record = new AnnouncementIgnore();
        record.setAnnouncementId(announcementId);
        record.setOpenid(openid);
        announcementIgnoreMapper.insert(record);
        log.info("用户忽略公告，userId={}, announcementId={}", userId, announcementId);
    }
}

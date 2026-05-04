package com.zesheng.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zesheng.admin.entity.Announcement;
import com.zesheng.admin.mapper.AnnouncementMapper;
import com.zesheng.admin.model.request.AnnouncementPageRequest;
import com.zesheng.admin.model.request.AnnouncementSaveRequest;
import com.zesheng.admin.model.request.AnnouncementUpdateRequest;
import com.zesheng.admin.service.IAnnouncementService;
import com.zesheng.common.response.R;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 公告 Service 实现
 */
@Service
public class AnnouncementServiceImpl implements IAnnouncementService {

    @Resource
    private AnnouncementMapper announcementMapper;

    @Override
    @Transactional(readOnly = true)
    public List<Announcement> list() {
        return announcementMapper.selectList(
                new LambdaQueryWrapper<Announcement>()
                        .orderByDesc(Announcement::getStatus)
                        .orderByDesc(Announcement::getUpdatedAt)
                        .orderByDesc(Announcement::getId));
    }

    @Override
    @Transactional(readOnly = true)
    public IPage<Announcement> page(AnnouncementPageRequest queryDto) {
        Page<Announcement> page = new Page<>(queryDto.getPageNum(), queryDto.getPageSize());
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        String keyword = queryDto.getTitleKeyword();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(Announcement::getTitle, keyword.trim());
        }
        String orderBy = queryDto.getOrderBy();
        if (orderBy != null && !orderBy.isBlank()) {
            boolean asc = "ASC".equalsIgnoreCase(queryDto.getOrder());
            if ("status".equals(orderBy)) {
                wrapper.orderBy(true, asc, Announcement::getStatus);
            } else if ("updatedAt".equals(orderBy)) {
                wrapper.orderBy(true, asc, Announcement::getUpdatedAt);
            } else {
                wrapper.orderBy(true, asc, Announcement::getUpdatedAt);
            }
        } else {
            wrapper.orderByDesc(Announcement::getStatus)
                    .orderByDesc(Announcement::getUpdatedAt)
                    .orderByDesc(Announcement::getId);
        }
        return announcementMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional(readOnly = true)
    public Announcement getById(Long id) {
        return announcementMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Announcement> save(AnnouncementSaveRequest request) {
        Announcement entity = new Announcement();
        entity.setTitle(request.getTitle().trim());
        entity.setContent(request.getContent());
        entity.setStatus(normalizeStatus(request.getStatus()));
        announcementMapper.insert(entity);
        if (entity.getStatus() == 1) {
            disableOtherAnnouncements(entity.getId());
        }
        return R.success(announcementMapper.selectById(entity.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Announcement> update(Long id, AnnouncementUpdateRequest request) {
        Announcement entity = announcementMapper.selectById(id);
        if (entity == null) {
            return R.error("公告不存在");
        }
        entity.setTitle(request.getTitle().trim());
        entity.setContent(request.getContent());
        entity.setStatus(normalizeStatus(request.getStatus()));
        announcementMapper.updateById(entity);
        if (entity.getStatus() == 1) {
            disableOtherAnnouncements(id);
        }
        return R.success(announcementMapper.selectById(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Announcement> enable(Long id) {
        Announcement entity = announcementMapper.selectById(id);
        if (entity == null) {
            return R.error("公告不存在");
        }
        disableOtherAnnouncements(id);
        entity.setStatus(1);
        announcementMapper.updateById(entity);
        return R.success(announcementMapper.selectById(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Integer> deleteById(Long id) {
        int rows = announcementMapper.deleteById(id);
        return R.success(rows);
    }

    private Integer normalizeStatus(Integer status) {
        return Integer.valueOf(1).equals(status) ? 1 : 0;
    }

    private void disableOtherAnnouncements(Long currentId) {
        announcementMapper.update(
                null,
                new LambdaUpdateWrapper<Announcement>()
                        .set(Announcement::getStatus, 0)
                        .ne(currentId != null, Announcement::getId, currentId)
                        .eq(Announcement::getStatus, 1));
    }
}

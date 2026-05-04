package com.zesheng.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zesheng.admin.entity.Announcement;
import com.zesheng.admin.model.request.AnnouncementSaveRequest;
import com.zesheng.admin.model.request.AnnouncementUpdateRequest;
import com.zesheng.admin.model.request.AnnouncementPageRequest;
import com.zesheng.common.response.R;

import java.util.List;

/**
 * 公告 Service 接口
 */
public interface IAnnouncementService {

    List<Announcement> list();

    IPage<Announcement> page(AnnouncementPageRequest queryDto);

    Announcement getById(Long id);

    R<Announcement> save(AnnouncementSaveRequest request);

    R<Announcement> update(Long id, AnnouncementUpdateRequest request);

    R<Announcement> enable(Long id);

    R<Integer> deleteById(Long id);
}

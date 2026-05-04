package com.zesheng.client.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zesheng.client.entity.Announcement;
import org.apache.ibatis.annotations.Mapper;

/**
 * 公告 Mapper
 */
@Mapper
public interface AnnouncementMapper extends BaseMapper<Announcement> {
}

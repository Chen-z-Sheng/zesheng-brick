package com.zesheng.client.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zesheng.client.entity.AnnouncementIgnore;
import org.apache.ibatis.annotations.Mapper;

/**
 * 公告忽略记录 Mapper
 */
@Mapper
public interface AnnouncementIgnoreMapper extends BaseMapper<AnnouncementIgnore> {
}

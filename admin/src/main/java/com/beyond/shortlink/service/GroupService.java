package com.beyond.shortlink.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.beyond.shortlink.dao.entity.GroupDO;

/**
 * 短链接分组接口层
 */
public interface GroupService extends IService<GroupDO> {

    /**
     * 新增短链接分组
     */
    void saveGroup(String groupName);
}
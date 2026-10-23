package com.beyond.shortlink.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beyond.shortlink.dao.entity.GroupDO;
import com.beyond.shortlink.dao.mapper.GroupMapper;
import com.beyond.shortlink.service.GroupService;
import org.springframework.stereotype.Service;

/**
 * 短链接分组接口实现层
 */
@Service
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDO> implements GroupService {


    @Override
    public void saveGroup(String groupName) {

        String gid;

        do {
            gid = RandomUtil.randomString(6);
        } while (hasGid(gid));

        baseMapper.insert(GroupDO.builder().gid(gid).name(groupName).sortOrder(0).build());

    }

    private boolean hasGid(String gid) {
        return  baseMapper
                .exists(Wrappers.lambdaQuery(GroupDO.class).eq(GroupDO::getGid, gid));
    }
}


package com.beyond.shortlink.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beyond.shortlink.common.biz.user.UserContext;
import com.beyond.shortlink.common.convention.exception.ClientException;
import com.beyond.shortlink.dao.entity.BaseDO;
import com.beyond.shortlink.dao.entity.GroupDO;
import com.beyond.shortlink.dao.mapper.GroupMapper;
import com.beyond.shortlink.dto.req.ShortLinkGroupSortReqDTO;
import com.beyond.shortlink.dto.req.ShortLinkGroupUpdateReqDTO;
import com.beyond.shortlink.dto.resp.ShortLinkGroupRespDTO;
import com.beyond.shortlink.service.GroupService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 短链接分组接口实现层
 */
@Service
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDO> implements GroupService {


    @Override
    public void saveGroup(String groupName) {
        saveGroup(UserContext.getUsername(), groupName);
    }

    @Override
    public void saveGroup(String username, String groupName) {
        String gid;
        do {
            gid = RandomUtil.randomString(6);
        } while (hasGid(username, gid));

        baseMapper.insert(GroupDO.builder().username(username).gid(gid).name(groupName).sortOrder(0).build());
    }



    @Override
    public List<ShortLinkGroupRespDTO> listGroup() {
        LambdaQueryWrapper<GroupDO> wrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .eq(GroupDO::getDelFlag, 0)
                .orderByAsc(GroupDO::getSortOrder)
                .orderByDesc(BaseDO::getUpdateTime);

        List<GroupDO> groupDOList = baseMapper.selectList(wrapper);

        return BeanUtil.copyToList(groupDOList, ShortLinkGroupRespDTO.class);

    }

    @Override
    public void updateGroup(ShortLinkGroupUpdateReqDTO requestParam) {
        LambdaUpdateWrapper<GroupDO> wrapper = Wrappers.lambdaUpdate(GroupDO.class)
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .eq(GroupDO::getGid, requestParam.getGid())
                .eq(GroupDO::getDelFlag, 0);

        GroupDO groupDO = GroupDO.builder()
                .name(requestParam.getName())
                .build();

        int updated = baseMapper.update(groupDO, wrapper);
        if (updated < 1) {
            throw new ClientException("短链接分组不存在");
        }
    }

    @Override
    public void deleteGroup(String gid) {
        LambdaUpdateWrapper<GroupDO> updateWrapper = Wrappers.lambdaUpdate(GroupDO.class)
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .eq(GroupDO::getGid, gid)
                .eq(GroupDO::getDelFlag, 0);

        GroupDO groupDO = new GroupDO();
        groupDO.setDelFlag(1);

        int updated = baseMapper.update(groupDO, updateWrapper);
        if (updated < 1) {
            throw new ClientException("短链接分组不存在");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sortGroup(List<ShortLinkGroupSortReqDTO> requestParam) {
        String username = UserContext.getUsername();

        requestParam.forEach(each -> {

            LambdaUpdateWrapper<GroupDO> wrapper = Wrappers.lambdaUpdate(GroupDO.class)
                    .eq(GroupDO::getDelFlag, 0)
                    .eq(GroupDO::getGid, each.getGid())
                    .eq(GroupDO::getUsername, username);

            GroupDO groupDO = GroupDO.builder().sortOrder(each.getSortOrder()).build();

            int updated = baseMapper.update(groupDO, wrapper);

            if (updated < 1) {
                throw new ClientException("短链接分组不存在");
            }


        });
    }

    private boolean hasGid(String username, String gid) {
        return baseMapper
                .exists(Wrappers.lambdaQuery(GroupDO.class)
                        .eq(GroupDO::getUsername, username)
                        .eq(GroupDO::getGid, gid));
    }
}


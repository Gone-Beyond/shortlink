package com.beyond.shortlink.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beyond.shortlink.common.constant.RedisKeyConstant;
import com.beyond.shortlink.common.convention.exception.ClientException;
import com.beyond.shortlink.dao.entity.LinkDO;
import com.beyond.shortlink.dao.mapper.LinkMapper;
import com.beyond.shortlink.dto.req.RecycleBinSaveReqDTO;
import com.beyond.shortlink.service.RecycleBinService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 回收站管理接口实现层
 */
@Service
@RequiredArgsConstructor
public class RecycleBinServiceImpl
        extends ServiceImpl<LinkMapper, LinkDO>
        implements RecycleBinService {

    private final StringRedisTemplate stringRedisTemplate;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveRecycleBin(RecycleBinSaveReqDTO requestParam) {

        LambdaUpdateWrapper<LinkDO> updateWrapper = Wrappers.lambdaUpdate(LinkDO.class)
                .eq(LinkDO::getGid, requestParam.getGid())
                .eq(LinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(LinkDO::getEnableStatus, 0)
                .eq(LinkDO::getDelFlag, 0);

        int updated = baseMapper.update(LinkDO.builder().enableStatus(1).build(), updateWrapper);

        if (updated < 1) {
            throw new ClientException("短链接不存在或已在回收站");
        }

        stringRedisTemplate.delete(
                String.format(RedisKeyConstant.GOTO_SHORT_LINK_KEY, requestParam.getFullShortUrl())
        );



    }
}
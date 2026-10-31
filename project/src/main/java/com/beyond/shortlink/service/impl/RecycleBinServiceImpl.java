package com.beyond.shortlink.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beyond.shortlink.common.convention.exception.ClientException;
import com.beyond.shortlink.dao.entity.LinkDO;
import com.beyond.shortlink.dao.mapper.LinkMapper;
import com.beyond.shortlink.dto.req.RecycleBinPageReqDTO;
import com.beyond.shortlink.dto.req.RecycleBinRecoverReqDTO;
import com.beyond.shortlink.dto.req.RecycleBinRemoveReqDTO;
import com.beyond.shortlink.dto.req.RecycleBinSaveReqDTO;
import com.beyond.shortlink.dto.resp.ShortLinkPageRespDTO;
import com.beyond.shortlink.service.RecycleBinService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.beyond.shortlink.common.constant.RedisKeyConstant.GOTO_SHORT_LINK_KEY;
import static com.beyond.shortlink.common.constant.RedisKeyConstant.IS_NULL_GOTO_SHORT_LINK_KEY;

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
                String.format(GOTO_SHORT_LINK_KEY, requestParam.getFullShortUrl())
        );



    }

    @Override
    public IPage<ShortLinkPageRespDTO> pageRecycleBin(
            RecycleBinPageReqDTO requestParam) {

        if (CollUtil.isEmpty(requestParam.getGidList())) {
            return new Page<>(
                    requestParam.getCurrent(),
                    requestParam.getSize(),
                    0);
        }

        LambdaQueryWrapper<LinkDO> queryWrapper =
                Wrappers.lambdaQuery(LinkDO.class)
                        .in(LinkDO::getGid, requestParam.getGidList())
                        .eq(LinkDO::getEnableStatus, 1)
                        .eq(LinkDO::getDelFlag, 0)
                        .orderByDesc(LinkDO::getCreateTime);

        IPage<LinkDO> resultPage =
                baseMapper.selectPage(requestParam, queryWrapper);

        return resultPage.convert(each ->
                BeanUtil.toBean(each, ShortLinkPageRespDTO.class));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void recoverRecycleBin(RecycleBinRecoverReqDTO requestParam) {
        LambdaUpdateWrapper<LinkDO> updateWrapper = Wrappers.lambdaUpdate(LinkDO.class)
                .eq(LinkDO::getGid, requestParam.getGid())
                .eq(LinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(LinkDO::getEnableStatus, 1)
                .eq(LinkDO::getDelFlag, 0)
                .set(LinkDO::getEnableStatus, 0);

        int updated = baseMapper.update(null, updateWrapper);
        if (updated < 1) {
            throw new ClientException("短链接不存在或不在回收站");
        }

        stringRedisTemplate.delete(
                String.format(GOTO_SHORT_LINK_KEY, requestParam.getFullShortUrl())
        );
        stringRedisTemplate.delete(
                String.format(IS_NULL_GOTO_SHORT_LINK_KEY, requestParam.getFullShortUrl())
        );
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void removeRecycleBin(RecycleBinRemoveReqDTO requestParam) {
        LambdaUpdateWrapper<LinkDO> updateWrapper = Wrappers.lambdaUpdate(LinkDO.class)
                .eq(LinkDO::getGid, requestParam.getGid())
                .eq(LinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(LinkDO::getEnableStatus, 1)
                .eq(LinkDO::getDelTime, 0L)
                .eq(LinkDO::getDelFlag, 0)
                .set(LinkDO::getDelTime, System.currentTimeMillis())
                .set(LinkDO::getDelFlag, 1);

        int updated = baseMapper.update(null, updateWrapper);
        if (updated < 1) {
            throw new ClientException("短链接不存在或不在回收站");
        }

        stringRedisTemplate.delete(
                String.format(GOTO_SHORT_LINK_KEY, requestParam.getFullShortUrl())
        );
    }
}

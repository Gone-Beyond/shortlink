package com.beyond.shortlink.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beyond.shortlink.dao.entity.LinkDO;
import com.beyond.shortlink.dao.mapper.LinkMapper;
import com.beyond.shortlink.dto.req.ShortLinkCreateReqDTO;
import com.beyond.shortlink.dto.resp.ShortLinkCreateRespDTO;
import com.beyond.shortlink.service.ShortLinkService;
import com.beyond.shortlink.toolkit.HashUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 短链接接口实现层
 */
@Slf4j
@Service
public class ShortLinkServiceImpl extends ServiceImpl<LinkMapper, LinkDO> implements ShortLinkService {

    @Override
    public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestParam) {
        String shortUri = generateSuffix(requestParam);
        String fullShortUrl = requestParam.getDomain() + "/" + shortUri;

        LinkDO linkDO = BeanUtil.toBean(requestParam, LinkDO.class);
        linkDO.setShortUri(shortUri);
        linkDO.setFullShortUrl(fullShortUrl);
        linkDO.setEnableStatus(0);
        baseMapper.insert(linkDO);

        return ShortLinkCreateRespDTO.builder()
                .gid(requestParam.getGid())
                .originUrl(requestParam.getOriginUrl())
                .fullShortUrl(fullShortUrl)
                .build();
    }

    private String generateSuffix(ShortLinkCreateReqDTO requestParam) {
        String originUrl = requestParam.getOriginUrl();
        //TODO  重复问题怎么办  这里我完全不懂  问题都不知道是什么  视频没看 太低效
        return HashUtil.hashToBase62(originUrl);
    }
}

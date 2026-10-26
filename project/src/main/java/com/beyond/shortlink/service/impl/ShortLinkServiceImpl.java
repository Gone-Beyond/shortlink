package com.beyond.shortlink.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.UUID;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beyond.shortlink.common.convention.exception.ServiceException;
import com.beyond.shortlink.dao.entity.LinkDO;
import com.beyond.shortlink.dao.mapper.LinkMapper;
import com.beyond.shortlink.dto.req.ShortLinkCreateReqDTO;
import com.beyond.shortlink.dto.resp.ShortLinkCreateRespDTO;
import com.beyond.shortlink.service.ShortLinkService;
import com.beyond.shortlink.toolkit.HashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

/**
 * 短链接接口实现层
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShortLinkServiceImpl extends ServiceImpl<LinkMapper, LinkDO> implements ShortLinkService {

    private final RBloomFilter<String> shortUriCreateCachePenetrationBloomFilter;

    @Override
    public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestParam) {
        String shortUri = generateSuffix(requestParam);
        String fullShortUrl = requestParam.getDomain() + "/" + shortUri;

        LinkDO linkDO = BeanUtil.toBean(requestParam, LinkDO.class);
        linkDO.setShortUri(shortUri);
        linkDO.setFullShortUrl(fullShortUrl);
        linkDO.setEnableStatus(0);

        try {
            baseMapper.insert(linkDO);
        } catch (DuplicateKeyException ex) {

            LambdaQueryWrapper<LinkDO> wrapper = Wrappers.lambdaQuery(LinkDO.class)
                    .eq(LinkDO::getFullShortUrl, fullShortUrl);

            if (baseMapper.selectOne(wrapper) != null) {
                // 库里有、布隆里没有：补进布隆，下次生成直接跳过
                shortUriCreateCachePenetrationBloomFilter.add(fullShortUrl);
            }
            log.warn("短链接：{} 重复入库", fullShortUrl);
            throw new ServiceException("短链接生成重复");


        }

        shortUriCreateCachePenetrationBloomFilter.add(fullShortUrl);

        return ShortLinkCreateRespDTO.builder()
                .gid(requestParam.getGid())
                .originUrl(requestParam.getOriginUrl())
                .fullShortUrl(fullShortUrl)
                .build();
    }

    private String generateSuffix(ShortLinkCreateReqDTO requestParam) {
        int retryCount = 0;
        while (retryCount < 10) {
            String salted = requestParam.getOriginUrl() + UUID.fastUUID();
            String shortUri = HashUtil.hashToBase62(salted);
            if (!shortUriCreateCachePenetrationBloomFilter.contains(
                    requestParam.getDomain() + "/" + shortUri
            )) {
                return shortUri;
            }
            retryCount++;
        }
        throw new ServiceException("短链接频繁生成，请稍后再试");
        //这里就是只是 生成短链接的短码 没有过Bloom 所以会重复
        // 怎么重复？ 1 真重复 （并发撞码  我知道什么意思  不要在这里废话） A， B base62 之后还就是一样的 2 假重复bloom要满了
        // bloom说没有那是真没有 因为属于自己的部分空位还在  但是说有
        // 那就不一定了  也许已经满了 那不管那个也是重复了的结果
        // 2 ---->  后台任务解决
        // 1-----> SQL DB  看看有没有  由于加盐了  只要只1不2（即bloom容量OK）那么
        // 攻击者想1-----> SQL DB 打崩 基本不可能  因为 + UUID.fastUUID();
    }
}

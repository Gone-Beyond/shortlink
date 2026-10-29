package com.beyond.shortlink.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.UUID;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beyond.shortlink.common.convention.exception.ClientException;
import com.beyond.shortlink.common.convention.exception.ServiceException;
import com.beyond.shortlink.dao.entity.LinkDO;
import com.beyond.shortlink.dao.entity.LinkGotoDO;
import com.beyond.shortlink.dao.mapper.LinkGotoMapper;
import com.beyond.shortlink.dao.mapper.LinkMapper;
import com.beyond.shortlink.dto.req.ShortLinkCreateReqDTO;
import com.beyond.shortlink.dto.req.ShortLinkPageReqDTO;
import com.beyond.shortlink.dto.req.ShortLinkUpdateReqDTO;
import com.beyond.shortlink.dto.resp.ShortLinkCreateRespDTO;
import com.beyond.shortlink.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.beyond.shortlink.dto.resp.ShortLinkPageRespDTO;
import com.beyond.shortlink.service.ShortLinkService;
import com.beyond.shortlink.toolkit.HashUtil;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 短链接接口实现层
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShortLinkServiceImpl extends ServiceImpl<LinkMapper, LinkDO> implements ShortLinkService {

    private final RBloomFilter<String> shortUriCreateCachePenetrationBloomFilter;

    private final LinkGotoMapper linkGotoMapper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestParam) {
        String shortUri = generateSuffix(requestParam);
        String fullShortUrl = requestParam.getDomain() + "/" + shortUri;

        LinkDO linkDO = BeanUtil.toBean(requestParam, LinkDO.class);
        linkDO.setShortUri(shortUri);
        linkDO.setFullShortUrl(fullShortUrl);
        linkDO.setEnableStatus(0);

        LinkGotoDO linkGotoDO = LinkGotoDO.builder()
                .gid(requestParam.getGid())
                .fullShortUrl(fullShortUrl)
                .build();

        try {
            baseMapper.insert(linkDO);
            linkGotoMapper.insert(linkGotoDO);
        } catch (DuplicateKeyException ex) {

            LambdaQueryWrapper<LinkGotoDO> wrapper = Wrappers.lambdaQuery(LinkGotoDO.class)
                    .eq(LinkGotoDO::getFullShortUrl, fullShortUrl);

            if (linkGotoMapper.selectOne(wrapper) != null) {
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


    @Override
    public IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO requestParam) {
        LambdaQueryWrapper<LinkDO> queryWrapper = Wrappers.lambdaQuery(LinkDO.class)
                .eq(LinkDO::getGid, requestParam.getGid())
                .eq(LinkDO::getEnableStatus, 0)
                .eq(LinkDO::getDelFlag, 0)
                .orderByDesc(LinkDO::getCreateTime);
        IPage<LinkDO> resultPage = baseMapper.selectPage(requestParam, queryWrapper);
        return resultPage.convert(each -> BeanUtil.toBean(each, ShortLinkPageRespDTO.class));
    }

    @Override
    public List<ShortLinkGroupCountQueryRespDTO> listGroupShortLinkCount(List<String> requestParam) {
        QueryWrapper<LinkDO> queryWrapper = Wrappers.query(new LinkDO())
                .select("gid as gid, count(*) as shortLinkCount")
                .in("gid", requestParam)
                .eq("enable_status", 0)
                .eq("del_flag", 0)
                .groupBy("gid");
        List<Map<String, Object>> shortLinkList = baseMapper.selectMaps(queryWrapper);
        return BeanUtil.copyToList(shortLinkList, ShortLinkGroupCountQueryRespDTO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateShortLink(ShortLinkUpdateReqDTO requestParam) {

        LambdaQueryWrapper<LinkDO> queryWrapper = Wrappers.lambdaQuery(LinkDO.class)
                .eq(LinkDO::getGid, requestParam.getOriginGid())
                .eq(LinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(LinkDO::getDelFlag, 0)
                .eq(LinkDO::getEnableStatus, 0);

        LinkDO hasLinkDO = baseMapper.selectOne(queryWrapper);

        if (hasLinkDO == null) {
            throw new ClientException("短链接记录不存在");

        }

        if (Objects.equals(requestParam.getOriginGid(), requestParam.getGid())) {
            // 2. 不换分组：在原表里直接 UPDATE
            LambdaUpdateWrapper<LinkDO> updateWrapper = Wrappers.lambdaUpdate(LinkDO.class)
                    .eq(LinkDO::getGid, requestParam.getOriginGid())
                    .eq(LinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                    .eq(LinkDO::getDelFlag, 0)
                    .eq(LinkDO::getEnableStatus, 0)
                    .set(Objects.equals(requestParam.getValidDateType(), 0), LinkDO::getValidDate, null);
            LinkDO linkDO = LinkDO.builder()
                    .originUrl(requestParam.getOriginUrl())
                    .describe(requestParam.getDescribe())
                    .validDateType(requestParam.getValidDateType())
                    .validDate(requestParam.getValidDate())
                    .build();
            baseMapper.update(linkDO, updateWrapper);
        } else {
            // 3. 换分组：先删旧表，再插新表
            LambdaQueryWrapper<LinkDO> deleteWrapper = Wrappers.lambdaQuery(LinkDO.class)
                    .eq(LinkDO::getGid, requestParam.getOriginGid())
                    .eq(LinkDO::getFullShortUrl, requestParam.getFullShortUrl());
            baseMapper.delete(deleteWrapper);

            hasLinkDO.setId(null);
            hasLinkDO.setGid(requestParam.getGid());
            hasLinkDO.setOriginUrl(requestParam.getOriginUrl());
            hasLinkDO.setDescribe(requestParam.getDescribe());
            hasLinkDO.setValidDateType(requestParam.getValidDateType());
            hasLinkDO.setValidDate(requestParam.getValidDate());
            baseMapper.insert(hasLinkDO);

            // 4. 路由表改 gid（按 full_short_url 分表，短码没变，不用搬家）
            LambdaUpdateWrapper<LinkGotoDO> gotoUpdateWrapper = Wrappers.lambdaUpdate(LinkGotoDO.class)
                    .eq(LinkGotoDO::getFullShortUrl, requestParam.getFullShortUrl())
                    .eq(LinkGotoDO::getGid, requestParam.getOriginGid())
                    .set(LinkGotoDO::getGid, requestParam.getGid());
            linkGotoMapper.update(null, gotoUpdateWrapper);


        }


    }

    @Override
    public void restoreUrl(String shortUri, ServletRequest request, ServletResponse response) {
        String fullShortUrl = request.getServerName() + "/" + shortUri;

        LambdaQueryWrapper<LinkGotoDO> gotoDOLambdaQueryWrapper = Wrappers.lambdaQuery(LinkGotoDO.class)
                .eq(LinkGotoDO::getFullShortUrl, fullShortUrl);

        LinkGotoDO linkGotoDO = linkGotoMapper.selectOne(gotoDOLambdaQueryWrapper);

        if (linkGotoDO == null) {
            throw new ClientException("短链接不存在");
        }

        LambdaQueryWrapper<LinkDO> linkDOLambdaQueryWrapper = Wrappers.lambdaQuery(LinkDO.class)
                .eq(LinkDO::getGid, linkGotoDO.getGid())
                .eq(LinkDO::getFullShortUrl, fullShortUrl)
                .eq(LinkDO::getEnableStatus, 0)
                .eq(LinkDO::getDelFlag, 0);

        LinkDO linkDO = baseMapper.selectOne(linkDOLambdaQueryWrapper);

        if (linkDO == null) {
            throw new ClientException("短链接不存在");
        }

        try {
            ((HttpServletResponse)response).sendRedirect(linkDO.getOriginUrl());
        } catch (IOException e) {
            throw new ServiceException("短链接调整失败");
        }
    }
}

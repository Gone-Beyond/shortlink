package com.beyond.shortlink.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.beyond.shortlink.dao.entity.LinkDO;
import com.beyond.shortlink.dto.req.ShortLinkCreateReqDTO;
import com.beyond.shortlink.dto.req.ShortLinkPageReqDTO;
import com.beyond.shortlink.dto.req.ShortLinkUpdateReqDTO;
import com.beyond.shortlink.dto.resp.ShortLinkCreateRespDTO;
import com.beyond.shortlink.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.beyond.shortlink.dto.resp.ShortLinkPageRespDTO;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import java.util.List;

/**
 * 短链接接口层
 */
public interface ShortLinkService extends IService<LinkDO> {

    /**
     * 创建短链接
     *
     * @param requestParam 创建短链接请求参数
     * @return 短链接创建信息
     */
    ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestParam);

    /**
     * 分页查询短链接
     */
    IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO requestParam);


    /**
     * 查询短链接分组内数量
     *
     * @param requestParam 分组标识集合
     */
    List<ShortLinkGroupCountQueryRespDTO> listGroupShortLinkCount(List<String> requestParam);


    /**
     * 修改短链接
     *
     * @param requestParam 修改短链接请求参数
     */
    void updateShortLink(ShortLinkUpdateReqDTO requestParam);

    /**
     * 短链接跳转
     *
     * @param shortUri 短码
     * @param request  请求（用来取域名）
     * @param response 响应（用来做重定向）
     */
    void restoreUrl(String shortUri, ServletRequest request, ServletResponse response);

    /**
     * 获取目标网站标题
     *
     * @param url 目标网站地址
     * @return 网站标题，获取失败时返回空字符串
     */
    String getTitleByUrl(String url);





}

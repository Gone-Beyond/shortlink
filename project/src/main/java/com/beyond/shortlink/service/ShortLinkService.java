package com.beyond.shortlink.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.beyond.shortlink.dao.entity.LinkDO;
import com.beyond.shortlink.dto.req.ShortLinkCreateReqDTO;
import com.beyond.shortlink.dto.resp.ShortLinkCreateRespDTO;

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
}

package com.beyond.shortlink.dto.resp;

import lombok.Builder;
import lombok.Data;

/**
 * 短链接创建响应对象
 */
@Data
@Builder
public class ShortLinkCreateRespDTO {

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 原始链接
     */
    private String originUrl;

    /**
     * 完整短链接
     */
    private String fullShortUrl;
}

package com.beyond.shortlink.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.beyond.shortlink.dao.entity.LinkDO;
import lombok.Data;

/**
 * 短链接分页请求参数
 */
@Data
public class ShortLinkPageReqDTO extends Page<LinkDO> {

    /**
     * 分组标识
     */
    private String gid;
}
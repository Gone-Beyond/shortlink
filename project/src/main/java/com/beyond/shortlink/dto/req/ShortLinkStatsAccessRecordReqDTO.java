package com.beyond.shortlink.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.beyond.shortlink.dao.entity.LinkAccessLogsDO;
import lombok.Data;

/** 单个短链接访问记录分页请求参数 */
@Data
public class ShortLinkStatsAccessRecordReqDTO extends Page<LinkAccessLogsDO> {
    private String fullShortUrl;
    private String gid;
    /** 开始日期，格式 yyyy-MM-dd */
    private String startDate;
    /** 结束日期，格式 yyyy-MM-dd */
    private String endDate;
}

package com.beyond.shortlink.service;

import com.beyond.shortlink.dto.req.ShortLinkStatsReqDTO;
import com.beyond.shortlink.dto.resp.ShortLinkStatsRespDTO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.beyond.shortlink.dto.req.ShortLinkStatsAccessRecordReqDTO;
import com.beyond.shortlink.dto.resp.ShortLinkStatsAccessRecordRespDTO;

/**
 * 短链接监控接口层
 */
public interface ShortLinkStatsService {

    /**
     * 获取单个短链接指定时间内监控数据
     *
     * @param requestParam 获取短链接监控数据入参
     * @return 短链接监控数据
     */
    ShortLinkStatsRespDTO oneShortLinkStats(ShortLinkStatsReqDTO requestParam);

    IPage<ShortLinkStatsAccessRecordRespDTO> shortLinkStatsAccessRecord(ShortLinkStatsAccessRecordReqDTO requestParam);
}

package com.beyond.shortlink.controller;

import com.beyond.shortlink.common.convention.result.Result;
import com.beyond.shortlink.remote.ShortLinkRemoteService;
import com.beyond.shortlink.remote.dto.req.ShortLinkStatsReqDTO;
import com.beyond.shortlink.remote.dto.resp.ShortLinkStatsRespDTO;
import com.beyond.shortlink.service.GroupService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.beyond.shortlink.remote.dto.req.ShortLinkStatsAccessRecordReqDTO;
import com.beyond.shortlink.remote.dto.resp.ShortLinkStatsAccessRecordRespDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 短链接监控后管控制层
 */
@RestController
@RequiredArgsConstructor
public class ShortLinkStatsController {

    private final GroupService groupService;

    /**
     * 后续重构为 SpringCloud Feign 调用
     */
    ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {
    };

    /**
     * 访问单个短链接指定时间内监控数据
     */
    @GetMapping("/api/short-link/admin/v1/stats")
    public Result<ShortLinkStatsRespDTO> shortLinkStats(ShortLinkStatsReqDTO requestParam) {
        groupService.checkGroupBelongToUser(requestParam.getGid());
        return shortLinkRemoteService.oneShortLinkStats(requestParam);
    }

    /** 分页查询单个短链接访问记录 */
    @GetMapping("/api/short-link/admin/v1/stats/access-record")
    public Result<Page<ShortLinkStatsAccessRecordRespDTO>> shortLinkStatsAccessRecord(ShortLinkStatsAccessRecordReqDTO requestParam) {
        groupService.checkGroupBelongToUser(requestParam.getGid());
        return shortLinkRemoteService.shortLinkStatsAccessRecord(requestParam);
    }
}

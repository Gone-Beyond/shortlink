package com.beyond.shortlink.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.beyond.shortlink.common.convention.result.Result;
import com.beyond.shortlink.remote.ShortLinkRemoteService;
import com.beyond.shortlink.remote.dto.req.RecycleBinPageReqDTO;
import com.beyond.shortlink.remote.dto.req.RecycleBinSaveReqDTO;
import com.beyond.shortlink.remote.dto.resp.ShortLinkPageRespDTO;
import com.beyond.shortlink.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 回收站管理控制层
 */
@RestController
@RequiredArgsConstructor
public class RecycleBinController {

    private final GroupService groupService;

    /**
     * 后续重构为 OpenFeign 调用
     */
    private final ShortLinkRemoteService shortLinkRemoteService =
            new ShortLinkRemoteService() {
            };

    /**
     * 将短链接移入回收站
     */
    @PostMapping("/api/short-link/admin/v1/recycle-bin/save")
    public Result<Void> saveRecycleBin(
            @RequestBody RecycleBinSaveReqDTO requestParam) {

        groupService.checkGroupBelongToUser(requestParam.getGid());
        return shortLinkRemoteService.saveRecycleBin(requestParam);
    }

    /**
     * 分页查询回收站短链接
     */
    @GetMapping("/api/short-link/admin/v1/recycle-bin/page")
    public Result<Page<ShortLinkPageRespDTO>> pageRecycleBin(
            RecycleBinPageReqDTO requestParam) {

        requestParam.setGidList(groupService.listGroupIds());
        return shortLinkRemoteService.pageRecycleBin(requestParam);
    }
}
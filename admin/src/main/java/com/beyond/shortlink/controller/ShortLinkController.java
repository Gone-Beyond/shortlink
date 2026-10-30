package com.beyond.shortlink.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.beyond.shortlink.common.convention.result.Result;
import com.beyond.shortlink.remote.ShortLinkRemoteService;
import com.beyond.shortlink.remote.dto.req.ShortLinkCreateReqDTO;
import com.beyond.shortlink.remote.dto.req.ShortLinkPageReqDTO;
import com.beyond.shortlink.remote.dto.req.ShortLinkUpdateReqDTO;
import com.beyond.shortlink.remote.dto.resp.ShortLinkCreateRespDTO;
import com.beyond.shortlink.remote.dto.resp.ShortLinkPageRespDTO;
import com.beyond.shortlink.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 短链接后管控制层
 */
@RestController
@RequiredArgsConstructor
public class ShortLinkController {


    private final GroupService groupService;

    /**
     * 后续重构为 SpringCloud Feign 调用
     */
    ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {
    };

    /**
     * 创建短链接
     */
    @PostMapping("/api/short-link/admin/v1/create")
    public Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO requestParam) {
        groupService.checkGroupBelongToUser(requestParam.getGid());
        return shortLinkRemoteService.createShortLink(requestParam);
    }

    /**
     * 分页查询短链接
     */
    @GetMapping("/api/short-link/admin/v1/page")
    public Result<Page<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO requestParam) {
        groupService.checkGroupBelongToUser(requestParam.getGid());
        return shortLinkRemoteService.pageShortLink(requestParam);
    }

    /**
     * 修改短链接
     */
    @PostMapping("/api/short-link/admin/v1/update")
    public Result<Void> updateShortLink(@RequestBody ShortLinkUpdateReqDTO requestParam) {
        groupService.checkGroupBelongToUser(requestParam.getOriginGid());
        groupService.checkGroupBelongToUser(requestParam.getGid());
        return shortLinkRemoteService.updateShortLink(requestParam);
    }

    /**
     * 获取目标网站标题
     */
    @GetMapping("/api/short-link/admin/v1/title")
    public Result<String> getTitleByUrl(@RequestParam("url") String url) {
        return shortLinkRemoteService.getTitleByUrl(url);
    }
}
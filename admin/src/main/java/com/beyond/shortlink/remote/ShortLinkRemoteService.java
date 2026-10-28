package com.beyond.shortlink.remote;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.beyond.shortlink.common.convention.result.Result;
import com.beyond.shortlink.remote.dto.req.ShortLinkCreateReqDTO;
import com.beyond.shortlink.remote.dto.req.ShortLinkPageReqDTO;
import com.beyond.shortlink.remote.dto.req.ShortLinkUpdateReqDTO;
import com.beyond.shortlink.remote.dto.resp.ShortLinkCreateRespDTO;
import com.beyond.shortlink.remote.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.beyond.shortlink.remote.dto.resp.ShortLinkPageRespDTO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 短链接中台远程调用服务
 */
public interface ShortLinkRemoteService {

    String BASE_URL = "http://127.0.0.1:8001";

    /**
     * 创建短链接
     */
    default Result<ShortLinkCreateRespDTO> createShortLink(ShortLinkCreateReqDTO requestParam) {
        String resultBodyStr = HttpUtil.post(BASE_URL + "/api/short-link/v1/create", JSON.toJSONString(requestParam));
        return JSON.parseObject(resultBodyStr, new TypeReference<>() {
        });
    }

    /**
     * 分页查询短链接
     */
    default Result<Page<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO requestParam) {
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("gid", requestParam.getGid());
        requestMap.put("current", requestParam.getCurrent());
        requestMap.put("size", requestParam.getSize());
        String resultPageStr = HttpUtil.get(BASE_URL + "/api/short-link/v1/page", requestMap);
        return JSON.parseObject(resultPageStr, new TypeReference<>() {
        });
    }


    /**
     * 查询分组短链接总量
     */
    default Result<List<ShortLinkGroupCountQueryRespDTO>> listGroupShortLinkCount(List<String> requestParam) {
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("requestParam", requestParam);
        String resultStr = HttpUtil.get(BASE_URL + "/api/short-link/v1/count", requestMap);
        return JSON.parseObject(resultStr, new TypeReference<>() {
        });


    }

    /**
     * 修改短链接
     */
    default Result<Void> updateShortLink(ShortLinkUpdateReqDTO requestParam) {
        String resultStr = HttpUtil.post(BASE_URL + "/api/short-link/v1/update", JSON.toJSONString(requestParam));
        return JSON.parseObject(resultStr, new TypeReference<>() {
        });
    }
}
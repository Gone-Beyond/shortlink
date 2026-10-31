package com.beyond.shortlink.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.beyond.shortlink.dao.entity.LinkDO;
import com.beyond.shortlink.dto.req.RecycleBinPageReqDTO;
import com.beyond.shortlink.dto.req.RecycleBinSaveReqDTO;
import com.beyond.shortlink.dto.resp.ShortLinkPageRespDTO;

/**
 * 回收站管理接口层
 */
public interface RecycleBinService extends IService<LinkDO> {

    /**
     * 保存回收站（将短链接移至回收站）
     *
     * @param requestParam 请求参数
     */
    void saveRecycleBin(RecycleBinSaveReqDTO requestParam);

    /**
     * 分页查询回收站短链接
     *
     * @param requestParam 分页请求参数
     * @return 回收站短链接分页结果
     */
    IPage<ShortLinkPageRespDTO> pageRecycleBin(
            RecycleBinPageReqDTO requestParam);
}
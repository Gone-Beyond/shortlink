package com.beyond.shortlink.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.beyond.shortlink.dao.entity.LinkDO;
import com.beyond.shortlink.dto.req.RecycleBinSaveReqDTO;

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
}
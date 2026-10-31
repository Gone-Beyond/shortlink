package com.beyond.shortlink.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.beyond.shortlink.dao.entity.LinkDO;
import lombok.Data;

import java.util.List;

/**
 * 回收站分页请求参数
 */
@Data
public class RecycleBinPageReqDTO extends Page<LinkDO> {

    /**
     * 当前用户拥有的分组标识集合
     */
    private List<String> gidList;
}
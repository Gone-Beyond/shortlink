package com.beyond.shortlink.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.beyond.shortlink.dao.entity.GroupDO;
import com.beyond.shortlink.dto.req.ShortLinkGroupSortReqDTO;
import com.beyond.shortlink.dto.req.ShortLinkGroupUpdateReqDTO;
import com.beyond.shortlink.dto.resp.ShortLinkGroupRespDTO;

import java.util.List;

/**
 * 短链接分组接口层
 */
public interface GroupService extends IService<GroupDO> {

    /**
     * 新增短链接分组
     */
    void saveGroup(String groupName);

    List<ShortLinkGroupRespDTO> listGroup();

    /**
     * 修改短链接分组
     */
    void updateGroup(ShortLinkGroupUpdateReqDTO requestParam);

    /**
     * 删除短链接分组
     *
     * @param gid 短链接分组标识
     */
    void deleteGroup(String gid);

    void sortGroup(List<ShortLinkGroupSortReqDTO> requestParam);
}
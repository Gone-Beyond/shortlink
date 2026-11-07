package com.beyond.shortlink.dto.req;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.beyond.shortlink.dao.entity.LinkAccessLogsDO;
import lombok.Data;
/** 分组访问记录分页请求参数 */
@Data
public class ShortLinkGroupStatsAccessRecordReqDTO extends Page<LinkAccessLogsDO> {
    private String gid;
    private String startDate;
    private String endDate;
}

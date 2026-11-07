package com.beyond.shortlink.remote.dto.req;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;
/** 分组访问记录分页请求参数 */
@Data
public class ShortLinkGroupStatsAccessRecordReqDTO extends Page<Object> {
    private String gid;
    private String startDate;
    private String endDate;
}

package com.beyond.shortlink.dto.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

/** 短链接访问记录响应参数 */
@Data
public class ShortLinkStatsAccessRecordRespDTO {
    private String uvType;
    private String browser;
    private String os;
    private String ip;
    private String network;
    private String device;
    private String locale;
    private String user;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}

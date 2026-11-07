package com.beyond.shortlink.dto.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 短链接分页返回参数
 */
@Data
public class ShortLinkPageRespDTO {

    private Long id;

    private String domain;

    private String shortUri;

    private String fullShortUrl;

    private String originUrl;

    private String gid;

    private Integer validDateType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime validDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    private String describe;

    /**
     * 网站图标地址
     */
    private String favicon;

    /**
     * 历史 PV
     */
    private Integer totalPv;

    /**
     * 历史 UV
     */
    private Integer totalUv;

    /**
     * 历史 UIP
     */
    private Integer totalUip;

    /**
     * 今日 PV
     */
    private Integer todayPv;

    /**
     * 今日 UV
     */
    private Integer todayUv;

    /**
     * 今日 UIP
     */
    private Integer todayUip;
}
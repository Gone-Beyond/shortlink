package com.beyond.shortlink.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 短链接每日访问统计实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_link_stats_today")
public class LinkStatsTodayDO extends BaseDO {

    private Long id;

    private String fullShortUrl;

    private LocalDate date;

    private Integer todayPv;

    private Integer todayUv;

    private Integer todayUip;
}

package com.beyond.shortlink.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.beyond.shortlink.dao.entity.LinkStatsTodayDO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;

/**
 * 短链接每日访问统计持久层
 */
public interface LinkStatsTodayMapper extends BaseMapper<LinkStatsTodayDO> {

    /** 新增或累加短链接当日访问统计 */
    @Insert("INSERT INTO t_link_stats_today " +
            "(full_short_url, date, today_pv, today_uv, today_uip, create_time, update_time, del_flag) " +
            "VALUES (#{fullShortUrl}, #{date}, 1, #{uv}, #{uip}, NOW(), NOW(), 0) " +
            "ON DUPLICATE KEY UPDATE today_pv = today_pv + 1, today_uv = today_uv + #{uv}, " +
            "today_uip = today_uip + #{uip}, update_time = NOW()")
    int incrementTodayStats(@Param("fullShortUrl") String fullShortUrl, @Param("date") LocalDate date,
            @Param("uv") int uv, @Param("uip") int uip);
}

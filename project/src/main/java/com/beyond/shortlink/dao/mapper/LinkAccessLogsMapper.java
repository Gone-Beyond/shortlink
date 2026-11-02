package com.beyond.shortlink.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.beyond.shortlink.dao.entity.LinkAccessLogsDO;
import com.beyond.shortlink.dto.req.ShortLinkStatsReqDTO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.HashMap;
import java.util.List;

/**
 * 访问日志监控持久层
 */
public interface LinkAccessLogsMapper extends BaseMapper<LinkAccessLogsDO> {

    /**
     * 根据短链接获取指定日期内高频访问IP数据
     */
    @Select("SELECT " +
            "    tlal.ip, " +
            "    COUNT(tlal.ip) AS cnt " +
            "FROM " +
            "    t_link tl INNER JOIN t_link_access_logs tlal ON tl.full_short_url = tlal.full_short_url " +
            "WHERE " +
            "    tlal.full_short_url = #{param.fullShortUrl} " +
            "    AND tl.gid = #{param.gid} " +
            "    AND tl.del_flag = 0 " +
            "    AND tlal.create_time >= #{param.startDate} " +
            "    AND tlal.create_time < DATE_ADD(#{param.endDate}, INTERVAL 1 DAY) " +
            "GROUP BY " +
            "    tlal.ip " +
            "ORDER BY " +
            "    cnt DESC " +
            "LIMIT 5;")
    List<HashMap<String, Object>> listTopIpByShortLink(@Param("param") ShortLinkStatsReqDTO requestParam);

    /**
     * 根据短链接获取指定日期内新旧访客数据
     */
    @Select("SELECT " +
            "    COALESCE(SUM(old_user), 0) AS oldUserCnt, " +
            "    COALESCE(SUM(new_user), 0) AS newUserCnt " +
            "FROM ( " +
            "    SELECT " +
            "        CASE WHEN COUNT(DISTINCT DATE(tlal.create_time)) > 1 THEN 1 ELSE 0 END AS old_user, " +
            "        CASE WHEN COUNT(DISTINCT DATE(tlal.create_time)) = 1 " +
            "             AND MAX(tlal.create_time) >= #{param.startDate} " +
            "             AND MAX(tlal.create_time) < DATE_ADD(#{param.endDate}, INTERVAL 1 DAY) THEN 1 ELSE 0 END AS new_user " +
            "    FROM " +
            "        t_link tl INNER JOIN t_link_access_logs tlal ON tl.full_short_url = tlal.full_short_url " +
            "    WHERE " +
            "        tlal.full_short_url = #{param.fullShortUrl} " +
            "        AND tl.gid = #{param.gid} " +
            "        AND tl.del_flag = 0 " +
            "    GROUP BY " +
            "        tlal.`user` " +
            ") AS user_counts;")
    HashMap<String, Object> findUvTypeCntByShortLink(@Param("param") ShortLinkStatsReqDTO requestParam);
}

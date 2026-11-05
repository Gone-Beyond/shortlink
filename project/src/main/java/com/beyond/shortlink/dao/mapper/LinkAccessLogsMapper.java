package com.beyond.shortlink.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.beyond.shortlink.dao.entity.LinkAccessLogsDO;
import com.beyond.shortlink.dto.req.ShortLinkStatsReqDTO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;

/**
 * 访问日志监控持久层
 */
public interface LinkAccessLogsMapper extends BaseMapper<LinkAccessLogsDO> {

    /** 查询分组内指定短链接的访问记录 */
    @Select("SELECT tlal.* FROM t_link_access_logs tlal " +
            "INNER JOIN t_link tl ON tl.full_short_url = tlal.full_short_url " +
            "WHERE tl.gid = #{gid} AND tl.full_short_url = #{fullShortUrl} " +
            "AND tl.del_flag = 0 AND tlal.del_flag = 0 " +
            "AND tlal.create_time >= #{startTime} AND tlal.create_time < #{endTime} " +
            "ORDER BY tlal.create_time DESC, tlal.id DESC")
    IPage<LinkAccessLogsDO> selectAccessRecordPage(IPage<LinkAccessLogsDO> page,
            @Param("gid") String gid, @Param("fullShortUrl") String fullShortUrl,
            @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /** 根据访客首次访问时间判断查询期间的新老访客 */
    @Select("<script> SELECT tlal.`user` AS visitor, " +
            "CASE WHEN MIN(tlal.create_time) >= #{startTime} AND MIN(tlal.create_time) &lt; #{endTime} " +
            "THEN '新访客' ELSE '老访客' END AS uvType " +
            "FROM t_link_access_logs tlal " +
            "WHERE tlal.full_short_url = #{fullShortUrl} AND tlal.del_flag = 0 " +
            "AND tlal.`user` IN " +
            "<foreach collection='users' item='user' open='(' separator=',' close=')'>#{user}</foreach> " +
            "GROUP BY tlal.`user` </script>")
    List<Map<String, Object>> selectVisitorTypes(@Param("fullShortUrl") String fullShortUrl,
            @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime,
            @Param("users") List<String> users);

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

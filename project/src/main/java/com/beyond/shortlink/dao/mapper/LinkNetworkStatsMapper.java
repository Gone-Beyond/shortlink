package com.beyond.shortlink.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.beyond.shortlink.dao.entity.LinkNetworkStatsDO;
import com.beyond.shortlink.dto.req.ShortLinkStatsReqDTO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 访问网络统计访问持久层
 */
public interface LinkNetworkStatsMapper extends BaseMapper<LinkNetworkStatsDO> {

    /**
     * 记录访问网络监控数据
     */
    @Insert("INSERT INTO t_link_network_stats (full_short_url, date, cnt, network, create_time, update_time, del_flag) " +
            "VALUES (#{linkNetworkStats.fullShortUrl}, #{linkNetworkStats.date}, #{linkNetworkStats.cnt}, #{linkNetworkStats.network}, NOW(), NOW(), 0) " +
            "ON DUPLICATE KEY UPDATE cnt = cnt + #{linkNetworkStats.cnt};")
    void shortLinkNetworkState(@Param("linkNetworkStats") LinkNetworkStatsDO linkNetworkStatsDO);

    /**
     * 根据短链接获取指定日期内访问网络监控数据
     */
    @Select("SELECT " +
            "    tlns.network, " +
            "    SUM(tlns.cnt) AS cnt " +
            "FROM " +
            "    t_link tl INNER JOIN t_link_network_stats tlns ON tl.full_short_url = tlns.full_short_url " +
            "WHERE " +
            "    tlns.full_short_url = #{param.fullShortUrl} " +
            "    AND tl.gid = #{param.gid} " +
            "    AND tl.del_flag = 0 " +
            "    AND tlns.date BETWEEN #{param.startDate} AND #{param.endDate} " +
            "GROUP BY " +
            "    tlns.network;")
    List<LinkNetworkStatsDO> listNetworkStatsByShortLink(@Param("param") ShortLinkStatsReqDTO requestParam);
}

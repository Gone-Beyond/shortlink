package com.beyond.shortlink.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.beyond.shortlink.dao.entity.LinkDO;
import com.beyond.shortlink.dto.resp.ShortLinkPageRespDTO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;

/**
 * 短链接持久层
 */
public interface LinkMapper extends BaseMapper<LinkDO> {

    /** 分页查询短链接及当日访问统计 */
    @Select("SELECT tl.*, COALESCE(today.today_pv, 0) AS today_pv, " +
            "COALESCE(today.today_uv, 0) AS today_uv, COALESCE(today.today_uip, 0) AS today_uip " +
            "FROM t_link tl LEFT JOIN t_link_stats_today today " +
            "ON tl.full_short_url = today.full_short_url AND today.date = #{date} AND today.del_flag = 0 " +
            "WHERE tl.gid = #{gid} AND tl.enable_status = 0 AND tl.del_flag = 0 " +
            "ORDER BY tl.create_time DESC, tl.id DESC")
    IPage<ShortLinkPageRespDTO> selectLinkPage(IPage<ShortLinkPageRespDTO> page,
            @Param("gid") String gid, @Param("date") LocalDate date);

    /** 累加短链接历史访问统计 */
    @Update("UPDATE t_link SET total_pv = total_pv + 1, total_uv = total_uv + #{uv}, " +
            "total_uip = total_uip + #{uip} WHERE gid = #{gid} " +
            "AND full_short_url = #{fullShortUrl} AND del_flag = 0 AND enable_status = 0")
    int incrementLinkStats(@Param("gid") String gid, @Param("fullShortUrl") String fullShortUrl,
            @Param("uv") int uv, @Param("uip") int uip);
}

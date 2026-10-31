package com.beyond.shortlink.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 短链接跳转路由实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_link_goto")
public class LinkGotoDO {

    /**
     * ID，由 MyBatis-Plus 分配雪花 ID
     */
    private Long id;

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 完整短链接
     */
    private String fullShortUrl;
}

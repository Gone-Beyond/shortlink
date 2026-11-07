package com.beyond.shortlink;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.beyond.shortlink.dao.entity.LinkDO;
import com.beyond.shortlink.dao.entity.LinkGotoDO;
import com.beyond.shortlink.dao.entity.LinkStatsTodayDO;
import com.beyond.shortlink.dao.mapper.LinkMapper;
import com.beyond.shortlink.dao.mapper.LinkGotoMapper;
import com.beyond.shortlink.dao.mapper.LinkStatsTodayMapper;
import com.beyond.shortlink.dto.req.ShortLinkPageReqDTO;
import com.beyond.shortlink.dto.req.ShortLinkStatsReqDTO;
import com.beyond.shortlink.dto.resp.ShortLinkPageRespDTO;
import com.beyond.shortlink.dto.resp.ShortLinkStatsRespDTO;
import com.beyond.shortlink.service.ShortLinkService;
import com.beyond.shortlink.service.ShortLinkStatsService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static com.beyond.shortlink.common.constant.RedisKeyConstant.*;
import static org.junit.jupiter.api.Assertions.*;

/** 依赖本地 MySQL、Redis 和开发配置，需显式指定运行 */
@SpringBootTest
class ShortLinkDailyStatsIT {
    @Autowired private ShortLinkService shortLinkService;
    @Autowired private ShortLinkStatsService shortLinkStatsService;
    @Autowired private LinkMapper linkMapper;
    @Autowired private LinkGotoMapper gotoMapper;
    @Autowired private LinkStatsTodayMapper todayMapper;
    @Autowired private StringRedisTemplate redis;
    @Autowired private JdbcTemplate jdbcTemplate;

    @Test
    void countsRepeatVisitsAndReturnsTodayStatsInPagedList() {
        String gid = "stats-it-" + UUID.randomUUID().toString().substring(0, 10);
        String url = "stats-it.invalid/" + UUID.randomUUID();
        LocalDate today = LocalDate.now();
        try {
            linkMapper.insert(LinkDO.builder().gid(gid).fullShortUrl(url).enableStatus(0).build());
            gotoMapper.insert(LinkGotoDO.builder().gid(gid).fullShortUrl(url).build());
            redis.opsForValue().set(String.format(GOTO_SHORT_LINK_KEY, url), "https://example.com");

            ShortLinkPageReqDTO request = new ShortLinkPageReqDTO();
            request.setGid(gid);
            ShortLinkPageRespDTO empty = shortLinkService.pageShortLink(request).getRecords().get(0);
            assertEquals(0, empty.getTodayPv());

            MockHttpServletResponse firstResponse = visit(url, "192.0.2.1", null);
            assertEquals("https://example.com", firstResponse.getRedirectedUrl());
            Cookie uvCookie = firstResponse.getCookie("uv");
            assertNotNull(uvCookie);
            visit(url, "192.0.2.1", uvCookie);
            visit(url, "192.0.2.2", null);
            redis.opsForSet().add(SHORT_LINK_STATS_UV_KEY + url, "existing-visitor");
            redis.opsForSet().add(SHORT_LINK_STATS_UIP_KEY + url, "192.0.2.3");
            visit(url, "192.0.2.3", new Cookie("uv", "existing-visitor"));

            LinkDO link = linkMapper.selectOne(Wrappers.lambdaQuery(LinkDO.class)
                    .eq(LinkDO::getGid, gid).eq(LinkDO::getFullShortUrl, url));
            assertEquals(4, link.getTotalPv());
            assertEquals(2, link.getTotalUv());
            assertEquals(2, link.getTotalUip());
            LinkStatsTodayDO daily = todayMapper.selectOne(Wrappers.lambdaQuery(LinkStatsTodayDO.class)
                    .eq(LinkStatsTodayDO::getFullShortUrl, url).eq(LinkStatsTodayDO::getDate, today));
            assertEquals(4, daily.getTodayPv());
            assertEquals(3, daily.getTodayUv());
            assertEquals(3, daily.getTodayUip());

            var result = shortLinkService.pageShortLink(request);
            assertEquals(1, result.getTotal());
            ShortLinkPageRespDTO actual = result.getRecords().get(0);
            assertEquals(4, actual.getTotalPv());
            assertEquals(2, actual.getTotalUv());
            assertEquals(2, actual.getTotalUip());
            assertEquals(4, actual.getTodayPv());
            assertEquals(3, actual.getTodayUv());
            assertEquals(3, actual.getTodayUip());

            ShortLinkStatsReqDTO statsRequest = new ShortLinkStatsReqDTO();
            statsRequest.setGid(gid);
            statsRequest.setFullShortUrl(url);
            statsRequest.setStartDate(today.toString());
            statsRequest.setEndDate(today.toString());
            ShortLinkStatsRespDTO stats = shortLinkStatsService.oneShortLinkStats(statsRequest);
            assertEquals(4, stats.getPv());
            assertEquals(3, stats.getUv());
            assertEquals(3, stats.getUip());
        } finally {
            linkMapper.delete(Wrappers.lambdaQuery(LinkDO.class).eq(LinkDO::getGid, gid));
            gotoMapper.delete(Wrappers.lambdaQuery(LinkGotoDO.class).eq(LinkGotoDO::getFullShortUrl, url));
            todayMapper.delete(Wrappers.lambdaQuery(LinkStatsTodayDO.class).eq(LinkStatsTodayDO::getFullShortUrl, url));
            for (String table : new String[]{"t_link_access_stats", "t_link_locale_stats", "t_link_os_stats",
                    "t_link_browser_stats", "t_link_device_stats", "t_link_network_stats", "t_link_access_logs"}) {
                jdbcTemplate.update("DELETE FROM " + table + " WHERE full_short_url = ?", url);
            }
            redis.delete(SHORT_LINK_STATS_UV_KEY + url);
            redis.delete(String.format(GOTO_SHORT_LINK_KEY, url));
            redis.delete(SHORT_LINK_STATS_UIP_KEY + url);
            redis.delete(SHORT_LINK_STATS_DAILY_UV_KEY + today + ":" + url);
            redis.delete(SHORT_LINK_STATS_DAILY_UIP_KEY + today + ":" + url);
        }
    }

    @Test
    void sortsBeforePagingAndIncludesLinksWithoutTodayStats() {
        String gid = "sort-it-" + UUID.randomUUID().toString().substring(0, 10);
        String prefix = "sort-it.invalid/" + UUID.randomUUID();
        String a = prefix + "a";
        String b = prefix + "b";
        String c = prefix + "c";
        try {
            linkMapper.insert(LinkDO.builder().gid(gid).fullShortUrl(a).enableStatus(0).totalPv(50).build());
            linkMapper.insert(LinkDO.builder().gid(gid).fullShortUrl(b).enableStatus(0).totalPv(100).build());
            linkMapper.insert(LinkDO.builder().gid(gid).fullShortUrl(c).enableStatus(0).totalPv(10).build());
            todayMapper.insert(LinkStatsTodayDO.builder().fullShortUrl(a).date(LocalDate.now())
                    .todayPv(20).todayUv(0).todayUip(0).build());
            todayMapper.insert(LinkStatsTodayDO.builder().fullShortUrl(b).date(LocalDate.now())
                    .todayPv(5).todayUv(0).todayUip(0).build());

            ShortLinkPageReqDTO request = new ShortLinkPageReqDTO();
            request.setGid(gid);
            request.setSize(2);
            request.setOrderTag("todayPv");
            assertEquals(List.of(a, b), shortLinkService.pageShortLink(request).getRecords().stream()
                    .map(ShortLinkPageRespDTO::getFullShortUrl).toList());
            request.setCurrent(2);
            ShortLinkPageRespDTO last = shortLinkService.pageShortLink(request).getRecords().get(0);
            assertEquals(c, last.getFullShortUrl());
            assertEquals(0, last.getTodayPv());

            request.setCurrent(1);
            request.setOrderTag("totalPv");
            assertEquals(List.of(b, a), shortLinkService.pageShortLink(request).getRecords().stream()
                    .map(ShortLinkPageRespDTO::getFullShortUrl).toList());
            request.setOrderTag("unknown");
            List<String> fallback = shortLinkService.pageShortLink(request).getRecords().stream()
                    .map(ShortLinkPageRespDTO::getFullShortUrl).toList();
            request.setOrderTag(null);
            assertEquals(fallback, shortLinkService.pageShortLink(request).getRecords().stream()
                    .map(ShortLinkPageRespDTO::getFullShortUrl).toList());
        } finally {
            linkMapper.delete(Wrappers.lambdaQuery(LinkDO.class).eq(LinkDO::getGid, gid));
            for (String url : List.of(a, b, c)) {
                todayMapper.delete(Wrappers.lambdaQuery(LinkStatsTodayDO.class)
                        .eq(LinkStatsTodayDO::getFullShortUrl, url));
            }
        }
    }

    private MockHttpServletResponse visit(String url, String ip, Cookie cookie) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr(ip);
        request.setServerName("stats-it.invalid");
        request.setServerPort(80);
        if (cookie != null) {
            request.setCookies(cookie);
        }
        MockHttpServletResponse response = new MockHttpServletResponse();
        shortLinkService.restoreUrl(url.substring(url.indexOf('/') + 1), request, response);
        return response;
    }
}

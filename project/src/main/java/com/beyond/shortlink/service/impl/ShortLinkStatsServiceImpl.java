package com.beyond.shortlink.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.beyond.shortlink.dao.entity.*;
import com.beyond.shortlink.dao.mapper.*;
import com.beyond.shortlink.dto.req.ShortLinkStatsReqDTO;
import com.beyond.shortlink.dto.resp.*;
import com.beyond.shortlink.service.ShortLinkStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * 短链接监控接口实现层
 */
@Service
@RequiredArgsConstructor
public class ShortLinkStatsServiceImpl implements ShortLinkStatsService {

    private final LinkAccessStatsMapper linkAccessStatsMapper;
    private final LinkLocaleStatsMapper linkLocaleStatsMapper;
    private final LinkAccessLogsMapper linkAccessLogsMapper;
    private final LinkBrowserStatsMapper linkBrowserStatsMapper;
    private final LinkOsStatsMapper linkOsStatsMapper;
    private final LinkDeviceStatsMapper linkDeviceStatsMapper;
    private final LinkNetworkStatsMapper linkNetworkStatsMapper;

    @Override
    public ShortLinkStatsRespDTO oneShortLinkStats(ShortLinkStatsReqDTO requestParam) {
        // 基础访问详情
        List<LinkAccessStatsDO> listStatsByShortLink = linkAccessStatsMapper.listStatsByShortLink(requestParam);
        if (listStatsByShortLink == null || listStatsByShortLink.isEmpty()) {
            return ShortLinkStatsRespDTO.builder()
                    .pv(0).uv(0).uip(0)
                    .daily(new ArrayList<>())
                    .localeCnStats(new ArrayList<>())
                    .hourStats(new ArrayList<>())
                    .topIpStats(new ArrayList<>())
                    .weekdayStats(new ArrayList<>())
                    .browserStats(new ArrayList<>())
                    .osStats(new ArrayList<>())
                    .uvTypeStats(new ArrayList<>())
                    .deviceStats(new ArrayList<>())
                    .networkStats(new ArrayList<>())
                    .build();
        }
        int pv = listStatsByShortLink.stream().mapToInt(LinkAccessStatsDO::getPv).sum();
        int uv = listStatsByShortLink.stream().mapToInt(LinkAccessStatsDO::getUv).sum();
        int uip = listStatsByShortLink.stream().mapToInt(LinkAccessStatsDO::getUip).sum();
        // 地区访问详情（仅国内）
        List<ShortLinkStatsLocaleCNRespDTO> localeCnStats = new ArrayList<>();
        List<LinkLocaleStatsDO> listedLocaleByShortLink = linkLocaleStatsMapper.listLocaleByShortLink(requestParam);
        int localeCnSum = listedLocaleByShortLink.stream().mapToInt(LinkLocaleStatsDO::getCnt).sum();
        listedLocaleByShortLink.forEach(each -> {
            double ratio = (double) each.getCnt() / localeCnSum;
            double actualRatio = Math.round(ratio * 100.0) / 100.0;
            localeCnStats.add(ShortLinkStatsLocaleCNRespDTO.builder()
                    .cnt(each.getCnt())
                    .locale(each.getProvince())
                    .ratio(actualRatio)
                    .build());
        });
        // 小时访问详情
        List<Integer> hourStats = new ArrayList<>();
        List<LinkAccessStatsDO> listHourStatsByShortLink = linkAccessStatsMapper.listHourStatsByShortLink(requestParam);
        for (int i = 0; i < 24; i++) {
            int hour = i;
            int hourCnt = listHourStatsByShortLink.stream()
                    .filter(each -> Objects.equals(each.getHour(), hour))
                    .findFirst()
                    .map(LinkAccessStatsDO::getPv)
                    .orElse(0);
            hourStats.add(hourCnt);
        }
        // 高频访问IP详情
        List<ShortLinkStatsTopIpRespDTO> topIpStats = new ArrayList<>();
        linkAccessLogsMapper.listTopIpByShortLink(requestParam).forEach(each ->
                topIpStats.add(ShortLinkStatsTopIpRespDTO.builder()
                        .ip(each.get("ip").toString())
                        .cnt(Integer.parseInt(each.get("cnt").toString()))
                        .build()));
        // 一周访问详情
        List<Integer> weekdayStats = new ArrayList<>();
        List<LinkAccessStatsDO> listWeekdayStatsByShortLink = linkAccessStatsMapper.listWeekdayStatsByShortLink(requestParam);
        for (int i = 1; i <= 7; i++) {
            int weekday = i;
            int weekdayCnt = listWeekdayStatsByShortLink.stream()
                    .filter(each -> Objects.equals(each.getWeekday(), weekday))
                    .findFirst()
                    .map(LinkAccessStatsDO::getPv)
                    .orElse(0);
            weekdayStats.add(weekdayCnt);
        }
        // 浏览器访问详情
        List<ShortLinkStatsBrowserRespDTO> browserStats = new ArrayList<>();
        List<LinkBrowserStatsDO> listedBrowserStats = linkBrowserStatsMapper.listBrowserStatsByShortLink(requestParam);
        int browserSum = listedBrowserStats.stream().mapToInt(LinkBrowserStatsDO::getCnt).sum();
        listedBrowserStats.forEach(each -> {
            double ratio = (double) each.getCnt() / browserSum;
            double actualRatio = Math.round(ratio * 100.0) / 100.0;
            browserStats.add(ShortLinkStatsBrowserRespDTO.builder()
                    .cnt(each.getCnt())
                    .browser(each.getBrowser())
                    .ratio(actualRatio)
                    .build());
        });
        // 操作系统访问详情
        List<ShortLinkStatsOsRespDTO> osStats = new ArrayList<>();
        List<LinkOsStatsDO> listedOsStats = linkOsStatsMapper.listOsStatsByShortLink(requestParam);
        int osSum = listedOsStats.stream().mapToInt(LinkOsStatsDO::getCnt).sum();
        listedOsStats.forEach(each -> {
            double ratio = (double) each.getCnt() / osSum;
            double actualRatio = Math.round(ratio * 100.0) / 100.0;
            osStats.add(ShortLinkStatsOsRespDTO.builder()
                    .cnt(each.getCnt())
                    .os(each.getOs())
                    .ratio(actualRatio)
                    .build());
        });
        // 访客访问类型详情
        List<ShortLinkStatsUvRespDTO> uvTypeStats = new ArrayList<>();
        HashMap<String, Object> findUvTypeCntByShortLink = linkAccessLogsMapper.findUvTypeCntByShortLink(requestParam);
        int oldUserCnt = Integer.parseInt(findUvTypeCntByShortLink.get("oldUserCnt").toString());
        int newUserCnt = Integer.parseInt(findUvTypeCntByShortLink.get("newUserCnt").toString());
        int uvSum = oldUserCnt + newUserCnt;
        double oldRatio = uvSum == 0 ? 0 : Math.round((double) oldUserCnt / uvSum * 100.0) / 100.0;
        double newRatio = uvSum == 0 ? 0 : Math.round((double) newUserCnt / uvSum * 100.0) / 100.0;
        uvTypeStats.add(ShortLinkStatsUvRespDTO.builder().uvType("newUser").cnt(newUserCnt).ratio(newRatio).build());
        uvTypeStats.add(ShortLinkStatsUvRespDTO.builder().uvType("oldUser").cnt(oldUserCnt).ratio(oldRatio).build());
        // 访问设备类型详情
        List<ShortLinkStatsDeviceRespDTO> deviceStats = new ArrayList<>();
        List<LinkDeviceStatsDO> listedDeviceStats = linkDeviceStatsMapper.listDeviceStatsByShortLink(requestParam);
        int deviceSum = listedDeviceStats.stream().mapToInt(LinkDeviceStatsDO::getCnt).sum();
        listedDeviceStats.forEach(each -> {
            double ratio = (double) each.getCnt() / deviceSum;
            double actualRatio = Math.round(ratio * 100.0) / 100.0;
            deviceStats.add(ShortLinkStatsDeviceRespDTO.builder()
                    .cnt(each.getCnt())
                    .device(each.getDevice())
                    .ratio(actualRatio)
                    .build());
        });
        // 访问网络类型详情
        List<ShortLinkStatsNetworkRespDTO> networkStats = new ArrayList<>();
        List<LinkNetworkStatsDO> listedNetworkStats = linkNetworkStatsMapper.listNetworkStatsByShortLink(requestParam);
        int networkSum = listedNetworkStats.stream().mapToInt(LinkNetworkStatsDO::getCnt).sum();
        listedNetworkStats.forEach(each -> {
            double ratio = (double) each.getCnt() / networkSum;
            double actualRatio = Math.round(ratio * 100.0) / 100.0;
            networkStats.add(ShortLinkStatsNetworkRespDTO.builder()
                    .cnt(each.getCnt())
                    .network(each.getNetwork())
                    .ratio(actualRatio)
                    .build());
        });
        return ShortLinkStatsRespDTO.builder()
                .pv(pv).uv(uv).uip(uip)
                .daily(BeanUtil.copyToList(listStatsByShortLink, ShortLinkStatsAccessDailyRespDTO.class))
                .localeCnStats(localeCnStats)
                .hourStats(hourStats)
                .topIpStats(topIpStats)
                .weekdayStats(weekdayStats)
                .browserStats(browserStats)
                .osStats(osStats)
                .uvTypeStats(uvTypeStats)
                .deviceStats(deviceStats)
                .networkStats(networkStats)
                .build();
    }
}

package com.beyond.shortlink.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.beyond.shortlink.common.convention.exception.ClientException;
import com.beyond.shortlink.dao.entity.*;
import com.beyond.shortlink.dao.mapper.*;
import com.beyond.shortlink.dto.req.ShortLinkGroupStatsReqDTO;
import com.beyond.shortlink.dto.req.ShortLinkStatsAccessRecordReqDTO;
import com.beyond.shortlink.dto.req.ShortLinkStatsReqDTO;
import com.beyond.shortlink.dto.resp.*;
import com.beyond.shortlink.service.ShortLinkStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

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
    public IPage<ShortLinkStatsAccessRecordRespDTO> shortLinkStatsAccessRecord(ShortLinkStatsAccessRecordReqDTO requestParam) {
        if (StrUtil.isBlank(requestParam.getGid()) || StrUtil.isBlank(requestParam.getFullShortUrl())
                || StrUtil.isBlank(requestParam.getStartDate()) || StrUtil.isBlank(requestParam.getEndDate())
                || requestParam.getCurrent() < 1 || requestParam.getSize() < 1) {
            throw new ClientException("短链接、分组、日期及分页参数不能为空或无效");
        }
        LocalDate startDate;
        LocalDate endDate;
        try {
            startDate = LocalDate.parse(requestParam.getStartDate());
            endDate = LocalDate.parse(requestParam.getEndDate());
        } catch (DateTimeParseException ex) {
            throw new ClientException("日期格式应为 yyyy-MM-dd");
        }
        if (endDate.isBefore(startDate) || endDate.equals(LocalDate.MAX)) {
            throw new ClientException("结束日期不能早于开始日期或超出有效范围");
        }
        LocalDateTime startTime = startDate.atStartOfDay();
        LocalDateTime endTime = endDate.plusDays(1).atStartOfDay();
        IPage<LinkAccessLogsDO> page = linkAccessLogsMapper.selectAccessRecordPage(requestParam,
                requestParam.getGid(), requestParam.getFullShortUrl(), startTime, endTime);
        List<String> users = page.getRecords().stream().map(LinkAccessLogsDO::getUser)
                .filter(StrUtil::isNotBlank).distinct().toList();
        Map<String, String> visitorTypes = users.isEmpty() ? Map.of() : linkAccessLogsMapper
                .selectVisitorTypes(requestParam.getFullShortUrl(), startTime, endTime, users).stream()
                .collect(Collectors.toMap(each -> each.get("visitor").toString(), each -> each.get("uvType").toString()));
        return page.convert(each -> {
            ShortLinkStatsAccessRecordRespDTO result = BeanUtil.toBean(each, ShortLinkStatsAccessRecordRespDTO.class);
            if (StrUtil.isNotBlank(each.getUser())) {
                result.setUvType(visitorTypes.get(each.getUser()));
            }
            return result;
        });
    }

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
        LinkAccessStatsDO pvUvUidStatsByShortLink = linkAccessLogsMapper.findPvUvUidStatsByShortLink(requestParam);
        int pv = Optional.ofNullable(pvUvUidStatsByShortLink).map(LinkAccessStatsDO::getPv).orElse(0);
        int uv = Optional.ofNullable(pvUvUidStatsByShortLink).map(LinkAccessStatsDO::getUv).orElse(0);
        int uip = Optional.ofNullable(pvUvUidStatsByShortLink).map(LinkAccessStatsDO::getUip).orElse(0);
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

    @Override
    public ShortLinkStatsRespDTO groupShortLinkStats(ShortLinkGroupStatsReqDTO requestParam) {
        List<LinkAccessStatsDO> listStatsByGroup = linkAccessStatsMapper.listStatsByGroup(requestParam);
        if (listStatsByGroup == null || listStatsByGroup.isEmpty()) {
            return ShortLinkStatsRespDTO.builder()
                    .pv(0).uv(0).uip(0)
                    .daily(new ArrayList<>())
                    .localeCnStats(new ArrayList<>())
                    .hourStats(new ArrayList<>())
                    .topIpStats(new ArrayList<>())
                    .weekdayStats(new ArrayList<>())
                    .browserStats(new ArrayList<>())
                    .osStats(new ArrayList<>())
                    .deviceStats(new ArrayList<>())
                    .networkStats(new ArrayList<>())
                    .build();
        }
        LinkAccessStatsDO pvUvUidStatsByGroup = linkAccessLogsMapper.findPvUvUidStatsByGroup(requestParam);
        int pv = Optional.ofNullable(pvUvUidStatsByGroup).map(LinkAccessStatsDO::getPv).orElse(0);
        int uv = Optional.ofNullable(pvUvUidStatsByGroup).map(LinkAccessStatsDO::getUv).orElse(0);
        int uip = Optional.ofNullable(pvUvUidStatsByGroup).map(LinkAccessStatsDO::getUip).orElse(0);
        // 地区访问详情（仅国内）
        List<ShortLinkStatsLocaleCNRespDTO> localeCnStats = new ArrayList<>();
        List<LinkLocaleStatsDO> listedLocaleByGroup = linkLocaleStatsMapper.listLocaleByGroup(requestParam);
        int localeCnSum = listedLocaleByGroup.stream().mapToInt(LinkLocaleStatsDO::getCnt).sum();
        listedLocaleByGroup.forEach(each -> {
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
        List<LinkAccessStatsDO> listHourStatsByGroup = linkAccessStatsMapper.listHourStatsByGroup(requestParam);
        for (int i = 0; i < 24; i++) {
            int hour = i;
            int hourCnt = listHourStatsByGroup.stream()
                    .filter(each -> Objects.equals(each.getHour(), hour))
                    .findFirst()
                    .map(LinkAccessStatsDO::getPv)
                    .orElse(0);
            hourStats.add(hourCnt);
        }
        // 高频访问IP详情
        List<ShortLinkStatsTopIpRespDTO> topIpStats = new ArrayList<>();
        linkAccessLogsMapper.listTopIpByGroup(requestParam).forEach(each ->
                topIpStats.add(ShortLinkStatsTopIpRespDTO.builder()
                        .ip(each.get("ip").toString())
                        .cnt(Integer.parseInt(each.get("cnt").toString()))
                        .build()));
        // 一周访问详情
        List<Integer> weekdayStats = new ArrayList<>();
        List<LinkAccessStatsDO> listWeekdayStatsByGroup = linkAccessStatsMapper.listWeekdayStatsByGroup(requestParam);
        for (int i = 1; i <= 7; i++) {
            int weekday = i;
            int weekdayCnt = listWeekdayStatsByGroup.stream()
                    .filter(each -> Objects.equals(each.getWeekday(), weekday))
                    .findFirst()
                    .map(LinkAccessStatsDO::getPv)
                    .orElse(0);
            weekdayStats.add(weekdayCnt);
        }
        // 浏览器访问详情
        List<ShortLinkStatsBrowserRespDTO> browserStats = new ArrayList<>();
        List<LinkBrowserStatsDO> listedBrowserStats = linkBrowserStatsMapper.listBrowserStatsByGroup(requestParam);
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
        List<LinkOsStatsDO> listedOsStats = linkOsStatsMapper.listOsStatsByGroup(requestParam);
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
        // 访问设备类型详情
        List<ShortLinkStatsDeviceRespDTO> deviceStats = new ArrayList<>();
        List<LinkDeviceStatsDO> listedDeviceStats = linkDeviceStatsMapper.listDeviceStatsByGroup(requestParam);
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
        List<LinkNetworkStatsDO> listedNetworkStats = linkNetworkStatsMapper.listNetworkStatsByGroup(requestParam);
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
                .daily(BeanUtil.copyToList(listStatsByGroup, ShortLinkStatsAccessDailyRespDTO.class))
                .localeCnStats(localeCnStats)
                .hourStats(hourStats)
                .topIpStats(topIpStats)
                .weekdayStats(weekdayStats)
                .browserStats(browserStats)
                .osStats(osStats)
                .deviceStats(deviceStats)
                .networkStats(networkStats)
                .build();
    }
}

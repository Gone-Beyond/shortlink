package com.beyond.shortlink.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.beyond.shortlink.common.convention.exception.ClientException;
import com.beyond.shortlink.dao.entity.LinkAccessLogsDO;
import com.beyond.shortlink.dao.mapper.*;
import com.beyond.shortlink.dto.req.ShortLinkStatsAccessRecordReqDTO;
import com.beyond.shortlink.dto.resp.ShortLinkStatsAccessRecordRespDTO;
import com.beyond.shortlink.service.impl.ShortLinkStatsServiceImpl;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ShortLinkStatsAccessRecordTest {
    private final LinkAccessLogsMapper logsMapper = mock(LinkAccessLogsMapper.class);
    private final ShortLinkStatsServiceImpl service = new ShortLinkStatsServiceImpl(
            mock(LinkAccessStatsMapper.class), mock(LinkLocaleStatsMapper.class), logsMapper,
            mock(LinkBrowserStatsMapper.class), mock(LinkOsStatsMapper.class),
            mock(LinkDeviceStatsMapper.class), mock(LinkNetworkStatsMapper.class));

    @Test
    void keepsPaginationAndUsesInclusiveEndDateForVisitorTypes() {
        ShortLinkStatsAccessRecordReqDTO request = request();
        request.setCurrent(2);
        request.setSize(2);
        request.setTotal(3);
        request.setRecords(List.of(LinkAccessLogsDO.builder().user("visitor-a").ip("127.0.0.1").build()));
        LocalDateTime start = LocalDateTime.of(2026, 11, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 11, 5, 0, 0);
        when(logsMapper.selectAccessRecordPage(request, "group-a", "example.test/abc", start, end))
                .thenReturn(request);
        when(logsMapper.selectVisitorTypes("example.test/abc", start, end, List.of("visitor-a")))
                .thenReturn(List.of(Map.of("visitor", "visitor-a", "uvType", "新访客")));

        IPage<ShortLinkStatsAccessRecordRespDTO> result = service.shortLinkStatsAccessRecord(request);

        assertEquals(2, result.getCurrent());
        assertEquals(2, result.getSize());
        assertEquals(3, result.getTotal());
        assertEquals("新访客", result.getRecords().get(0).getUvType());
        assertEquals("127.0.0.1", result.getRecords().get(0).getIp());
    }

    @Test
    void rejectsReversedDateRangeBeforeQuerying() {
        ShortLinkStatsAccessRecordReqDTO request = request();
        request.setStartDate("2026-11-05");
        assertThrows(ClientException.class, () -> service.shortLinkStatsAccessRecord(request));
        verifyNoInteractions(logsMapper);
    }

    @Test
    void emptyPageDoesNotQueryVisitorTypes() {
        ShortLinkStatsAccessRecordReqDTO request = request();
        when(logsMapper.selectAccessRecordPage(eq(request), anyString(), anyString(), any(), any()))
                .thenReturn(request);
        assertTrue(service.shortLinkStatsAccessRecord(request).getRecords().isEmpty());
        verify(logsMapper, never()).selectVisitorTypes(anyString(), any(), any(), anyList());
    }

    private ShortLinkStatsAccessRecordReqDTO request() {
        ShortLinkStatsAccessRecordReqDTO request = new ShortLinkStatsAccessRecordReqDTO();
        request.setGid("group-a");
        request.setFullShortUrl("example.test/abc");
        request.setStartDate("2026-11-01");
        request.setEndDate("2026-11-04");
        return request;
    }
}

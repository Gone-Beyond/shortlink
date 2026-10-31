package com.beyond.shortlink;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.beyond.shortlink.dao.entity.LinkDO;
import com.beyond.shortlink.dao.entity.LinkGotoDO;
import com.beyond.shortlink.dao.mapper.LinkGotoMapper;
import com.beyond.shortlink.dao.mapper.LinkMapper;
import com.beyond.shortlink.dto.req.ShortLinkCreateReqDTO;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

/**
 * 依赖本地 MySQL、Redis 和已排除出 Git 的开发配置，显式指定测试类运行。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ShortLinkShardingIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private LinkMapper linkMapper;

    @SpyBean
    private LinkGotoMapper linkGotoMapper;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    void createEndpointWritesBothTables() {
        String gid = "it-" + UUID.randomUUID().toString().substring(0, 12);
        String fullShortUrl = null;
        try {
            JsonNode response = restTemplate.postForObject("/api/short-link/v1/create", request(gid), JsonNode.class);
            assertEquals("0", response.path("code").asText(), response.toString());
            fullShortUrl = response.path("data").path("fullShortUrl").asText();
            assertFalse(fullShortUrl.isEmpty());
            LinkDO link = linkMapper.selectOne(Wrappers.lambdaQuery(LinkDO.class).eq(LinkDO::getGid, gid));
            LinkGotoDO route = linkGotoMapper.selectOne(Wrappers.lambdaQuery(LinkGotoDO.class)
                    .eq(LinkGotoDO::getFullShortUrl, fullShortUrl));
            assertNotNull(link);
            assertNotNull(route);
            assertEquals(fullShortUrl, link.getFullShortUrl());
            assertEquals(gid, route.getGid());
            assertNotNull(route.getId());
        } finally {
            deleteLinks(gid);
            if (fullShortUrl != null) {
                deleteRoute(fullShortUrl);
            }
        }
    }

    @Test
    void routeInsertFailureRollsBackMainTable() {
        String gid = "it-" + UUID.randomUUID().toString().substring(0, 12);
        doThrow(new DuplicateKeyException("模拟路由表唯一索引冲突"))
                .when(linkGotoMapper).insert(any(LinkGotoDO.class));
        try {
            JsonNode response = restTemplate.postForObject("/api/short-link/v1/create", request(gid), JsonNode.class);
            assertNotEquals("0", response.path("code").asText());
            assertEquals(0L, linkMapper.selectCount(Wrappers.lambdaQuery(LinkDO.class).eq(LinkDO::getGid, gid)));
        } finally {
            deleteLinks(gid);
        }
    }

    @Test
    void routeUniqueIndexRejectsSameUrlAcrossGroups() {
        String url = "sharding-it.invalid/" + UUID.randomUUID();
        String firstGid = "sharding-it-a";
        String secondGid = "sharding-it-b";
        try {
            transactionTemplate.executeWithoutResult(status -> insertPair(firstGid, url));
            assertThrows(DuplicateKeyException.class,
                    () -> transactionTemplate.executeWithoutResult(status -> insertPair(secondGid, url)));
            assertEquals(0L, linkMapper.selectCount(Wrappers.lambdaQuery(LinkDO.class).eq(LinkDO::getGid, secondGid)));
            assertEquals(firstGid, linkGotoMapper.selectOne(Wrappers.lambdaQuery(LinkGotoDO.class)
                    .eq(LinkGotoDO::getFullShortUrl, url)).getGid());
        } finally {
            deleteLinks(firstGid);
            deleteLinks(secondGid);
            deleteRoute(url);
        }
    }

    private void insertPair(String gid, String url) {
        linkMapper.insert(LinkDO.builder().gid(gid).fullShortUrl(url).build());
        linkGotoMapper.insert(LinkGotoDO.builder().gid(gid).fullShortUrl(url).build());
    }

    private ShortLinkCreateReqDTO request(String gid) {
        ShortLinkCreateReqDTO request = new ShortLinkCreateReqDTO();
        request.setDomain("sharding-it.invalid");
        request.setOriginUrl("https://example.com");
        request.setGid(gid);
        request.setCreatedType(1);
        request.setValidDateType(0);
        return request;
    }

    private void deleteLinks(String gid) {
        linkMapper.delete(Wrappers.lambdaQuery(LinkDO.class).eq(LinkDO::getGid, gid));
    }

    private void deleteRoute(String url) {
        linkGotoMapper.delete(Wrappers.lambdaQuery(LinkGotoDO.class).eq(LinkGotoDO::getFullShortUrl, url));
    }
}

package com.zesheng.client.service.impl;

import com.zesheng.client.service.IRecycleMarketService;
import com.zesheng.common.constant.InternalApiHeaders;
import com.zesheng.common.enums.ResultCodeEnum;
import com.zesheng.common.exception.BizException;
import com.zesheng.common.model.response.recyclemarket.*;
import com.zesheng.common.response.R;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/**
 * 回收行情：转发至管理端数据域，C 端不直连品类/行情表
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecycleMarketServiceImpl implements IRecycleMarketService {

    private final RestTemplate restTemplate;

    @Value("${zesheng.admin.api-base}")
    private String adminApiBase;

    @Value("${zesheng.internal.recycle-market-api-key:}")
    private String recycleMarketInternalApiKey;

    @Override
    public List<RecycleMarketCategoryVo> listCategories() {
        URI uri = UriComponentsBuilder.fromHttpUrl(adminApiBase + "/pub/recycle-market/categories")
                .build()
                .encode()
                .toUri();
        return exchangeList(uri, new ParameterizedTypeReference<R<List<RecycleMarketCategoryVo>>>() {
        });
    }

    @Override
    public List<RecycleMarketSubCategoryVo> listSubCategories(Long level1Id) {
        URI uri = UriComponentsBuilder.fromHttpUrl(adminApiBase + "/pub/recycle-market/sub-categories")
                .queryParam("level1Id", level1Id)
                .build()
                .encode()
                .toUri();
        return exchangeList(uri, new ParameterizedTypeReference<R<List<RecycleMarketSubCategoryVo>>>() {
        });
    }

    @Override
    public List<RecycleMarketThirdCategoryVo> listThirdCategories(Long level2Id) {
        URI uri = UriComponentsBuilder.fromHttpUrl(adminApiBase + "/pub/recycle-market/third-categories")
                .queryParam("level2Id", level2Id)
                .build()
                .encode()
                .toUri();
        return exchangeList(uri, new ParameterizedTypeReference<R<List<RecycleMarketThirdCategoryVo>>>() {
        });
    }

    @Override
    public List<RecycleMarketProductVo> listProducts(Long level1Id, Long level2Id, Long level3Id, LocalDate priceDate) {
        UriComponentsBuilder b = UriComponentsBuilder.fromHttpUrl(adminApiBase + "/pub/recycle-market/products");
        if (level1Id != null) {
            b.queryParam("level1Id", level1Id);
        }
        if (level2Id != null) {
            b.queryParam("level2Id", level2Id);
        }
        if (level3Id != null) {
            b.queryParam("level3Id", level3Id);
        }
        if (priceDate != null) {
            b.queryParam("priceDate", priceDate);
        }
        URI uri = b.build().encode().toUri();
        return exchangeList(uri, new ParameterizedTypeReference<R<List<RecycleMarketProductVo>>>() {
        });
    }

    @Override
    public RecycleMarketProductVo getProductDetail(Long level3Id, LocalDate priceDate) {
        UriComponentsBuilder b = UriComponentsBuilder.fromHttpUrl(adminApiBase + "/pub/recycle-market/product/detail");
        if (level3Id != null) {
            b.queryParam("level3Id", level3Id);
        }
        if (priceDate != null) {
            b.queryParam("priceDate", priceDate);
        }
        URI uri = b.build().encode().toUri();
        return unwrapObject(uri, new ParameterizedTypeReference<R<RecycleMarketProductVo>>() {
        });
    }

    @Override
    public List<RecycleMarketPriceHistoryVo> getPriceHistory(Long level3Id, int limit) {
        UriComponentsBuilder b = UriComponentsBuilder.fromHttpUrl(adminApiBase + "/pub/recycle-market/price-history")
                .queryParam("limit", limit);
        if (level3Id != null) {
            b.queryParam("level3Id", level3Id);
        }
        URI uri = b.build().encode().toUri();
        return exchangeList(uri, new ParameterizedTypeReference<R<List<RecycleMarketPriceHistoryVo>>>() {
        });
    }

    private <T> List<T> exchangeList(URI uri, ParameterizedTypeReference<R<List<T>>> typeRef) {
        List<T> data = unwrapObject(uri, typeRef);
        return data != null ? data : Collections.emptyList();
    }

    private HttpEntity<Void> recycleMarketHttpEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(InternalApiHeaders.RECYCLE_MARKET_INTERNAL_KEY, recycleMarketInternalApiKey);
        return new HttpEntity<>(headers);
    }

    private <T> T unwrapObject(URI uri, ParameterizedTypeReference<R<T>> typeRef) {
        try {
            ResponseEntity<R<T>> resp = restTemplate.exchange(uri, HttpMethod.GET, recycleMarketHttpEntity(), typeRef);
            R<T> body = resp.getBody();
            if (body == null) {
                throw new BizException(ResultCodeEnum.THIRD_PARTY_ERROR, "管理端行情接口返回空响应");
            }
            if (!ResultCodeEnum.SUCCESS.getCode().equals(body.getCode())) {
                log.warn("管理端行情业务码非成功: uri={} code={} msg={}", uri, body.getCode(), body.getMsg());
                throw new BizException(ResultCodeEnum.THIRD_PARTY_ERROR,
                        body.getMsg() != null ? body.getMsg() : "管理端行情查询失败");
            }
            return body.getData();
        } catch (RestClientException e) {
            log.error("调用管理端行情接口失败: {}", uri, e);
            throw new BizException(ResultCodeEnum.THIRD_PARTY_ERROR, "行情服务暂不可用，请稍后重试", e);
        }
    }
}

package com.zesheng.common.kuaidi100;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kuaidi100.sdk.api.AutoNum;
import com.kuaidi100.sdk.api.QueryTrack;
import com.kuaidi100.sdk.pojo.HttpResult;
import com.kuaidi100.sdk.request.AutoNumReq;
import com.kuaidi100.sdk.request.QueryTrackParam;
import com.kuaidi100.sdk.request.QueryTrackReq;
import com.kuaidi100.sdk.response.AutoNumResp;
import com.kuaidi100.sdk.utils.SignUtils;
import com.zesheng.common.enums.ResultCodeEnum;
import com.zesheng.common.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 快递100 接口封装（实时查询、智能识别、订阅回调签名校验）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class Kuaidi100ClientUtil {

    private final Kuaidi100Properties kuaidi100Properties;

    private final Gson gson = new Gson();

    /**
     * 实时物流查询：返回原始 JSON 字符串，由业务层用 {@link Kuaidi100TraceAssembler} 按泛型 DTO 解析，
     * 避免 SDK 中 QueryTrackResp 的 raw List data 导致轨迹列表反序列化失败。
     */
    public String queryTrackRealtimeRawJson(String comCode, String trackingNo, String phone) throws Exception {
        assertConfigured();
        String com = StringUtils.trimToEmpty(comCode);
        String num = StringUtils.trimToEmpty(trackingNo);
        if (StringUtils.isAnyBlank(com, num)) {
            throw new IllegalArgumentException("快递公司编码与运单号不能为空");
        }

        QueryTrackParam queryTrackParam = new QueryTrackParam();
        queryTrackParam.setCom(com);
        queryTrackParam.setNum(num);
        if (StringUtils.isNotBlank(phone)) {
            queryTrackParam.setPhone(phone.trim());
        }

        String paramJson = gson.toJson(queryTrackParam);
        QueryTrackReq queryTrackReq = new QueryTrackReq();
        queryTrackReq.setParam(paramJson);
        queryTrackReq.setCustomer(kuaidi100Properties.getCustomer());
        queryTrackReq.setSign(SignUtils.querySign(paramJson, kuaidi100Properties.getKey(),
                kuaidi100Properties.getCustomer()));

        HttpResult httpResult = new QueryTrack().execute(queryTrackReq);
        if (httpResult == null || httpResult.getStatus() != HttpStatus.SC_OK
                || StringUtils.isBlank(httpResult.getBody())) {
            log.warn("快递100实时查询 HTTP 异常: com={}, num={}, status={}", com, num,
                    httpResult != null ? httpResult.getStatus() : null);
            throw new BizException(ResultCodeEnum.THIRD_PARTY_ERROR, "快递100查询失败或返回为空");
        }
        return httpResult.getBody();
    }

    /**
     * 智能识别运单号归属快递公司
     */
    public List<AutoNumResp> recognizeCompaniesByWaybillNo(String trackingNo) throws Exception {
        assertConfigured();
        String num = StringUtils.trimToEmpty(trackingNo);
        if (StringUtils.isBlank(num)) {
            throw new IllegalArgumentException("运单号不能为空");
        }

        AutoNumReq autoNumReq = new AutoNumReq();
        autoNumReq.setKey(kuaidi100Properties.getKey());
        autoNumReq.setNum(num);

        HttpResult httpResult = new AutoNum().execute(autoNumReq);
        if (httpResult == null || httpResult.getStatus() != HttpStatus.SC_OK
                || StringUtils.isBlank(httpResult.getBody())) {
            log.warn("快递100智能识别请求失败: status={}, num={}",
                    httpResult != null ? httpResult.getStatus() : null, num);
            return Collections.emptyList();
        }

        List<AutoNumResp> list = gson.fromJson(httpResult.getBody(),
                new TypeToken<List<AutoNumResp>>() { }.getType());
        return list != null ? list : Collections.emptyList();
    }

    /**
     * 订阅推送签名校验
     */
    public boolean verifySubscribeCallbackSign(String param, String sign) {
        if (StringUtils.isBlank(sign)) {
            return false;
        }
        String salt = kuaidi100Properties.getSubscribeSalt() != null
                ? kuaidi100Properties.getSubscribeSalt()
                : "";
        String ours = SignUtils.sign(StringUtils.defaultString(param) + salt);
        return sign.equalsIgnoreCase(ours);
    }

    public boolean isConfigured() {
        return kuaidi100Properties.isEnabled()
                && StringUtils.isNotBlank(kuaidi100Properties.getKey())
                && StringUtils.isNotBlank(kuaidi100Properties.getCustomer());
    }

    private void assertConfigured() {
        if (!kuaidi100Properties.isEnabled()) {
            throw new BizException(ResultCodeEnum.PARAM_ERROR, "快递100未启用或未配置");
        }
        if (StringUtils.isAnyBlank(kuaidi100Properties.getKey(), kuaidi100Properties.getCustomer())) {
            throw new BizException(ResultCodeEnum.PARAM_ERROR, "快递100未正确配置 key、customer");
        }
    }
}

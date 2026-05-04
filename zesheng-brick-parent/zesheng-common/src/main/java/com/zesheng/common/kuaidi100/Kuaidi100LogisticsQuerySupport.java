package com.zesheng.common.kuaidi100;

import com.kuaidi100.sdk.response.AutoNumResp;
import com.zesheng.common.dto.logistics.LogisticsTraceVo;
import com.zesheng.common.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;

/**
 * 物流查询编排：名称匹配 com → 智能识别 → 实时查询
 */
@Slf4j
public final class Kuaidi100LogisticsQuerySupport {

    private Kuaidi100LogisticsQuerySupport() {
    }

    public static LogisticsTraceVo queryTrace(
            String logisticsCompanyDisplayName,
            String trackingNo,
            String phoneForCourier,
            LogisticsComCodeResolver nameToCode,
            Kuaidi100ClientUtil client) {
        if (!client.isConfigured()) {
            return LogisticsTraceVo.fail("物流查询未配置");
        }
        String no = StringUtils.trimToEmpty(trackingNo);
        if (no.isEmpty()) {
            return LogisticsTraceVo.fail("缺少快递单号");
        }
        try {
            Optional<String> com = Optional.empty();
            if (StringUtils.isNotBlank(logisticsCompanyDisplayName) && nameToCode != null) {
                com = nameToCode.resolveByCompanyName(logisticsCompanyDisplayName.trim());
            }
            if (com.isEmpty()) {
                List<AutoNumResp> recognized = client.recognizeCompaniesByWaybillNo(no);
                if (!recognized.isEmpty() && StringUtils.isNotBlank(recognized.get(0).getComCode())) {
                    com = Optional.of(Kuaidi100TraceAssembler.normalizeComCode(recognized.get(0).getComCode()));
                }
            } else {
                com = com.map(Kuaidi100TraceAssembler::normalizeComCode);
            }
            if (com.isEmpty()) {
                return LogisticsTraceVo.fail("无法识别快递公司，请核对单号或物流公司名称");
            }
            String comCode = com.get();
            String phone = StringUtils.trimToNull(phoneForCourier);
            String rawJson = client.queryTrackRealtimeRawJson(comCode, no, phone);
            return Kuaidi100TraceAssembler.fromPollResponseJson(rawJson, no, comCode);
        } catch (BizException | IllegalArgumentException e) {
            return LogisticsTraceVo.fail(e.getMessage());
        } catch (Exception e) {
            log.warn("快递100查询异常, no={}", no, e);
            return LogisticsTraceVo.fail("物流查询异常，请稍后重试");
        }
    }
}

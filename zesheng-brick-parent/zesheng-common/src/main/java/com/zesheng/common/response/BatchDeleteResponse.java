package com.zesheng.common.response;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 全项目通用的批量删除响应类
 * 适配所有业务模块的批量删除接口，统一返回删除结果，无任何业务耦合
 */
@Data
public class BatchDeleteResponse implements Serializable {

    /**
     * 请求删除的ID总数
     * 前端传入的待删除ID数量，用于核对总数
     */
    private Integer totalRequest;

    /**
     * 成功删除的数量
     * 默认值为0，避免空指针异常
     */
    private Integer successCount = 0;

    /**
     * 删除失败的数量
     * 默认值为0，避免空指针异常
     */
    private Integer failedCount = 0;

    /**
     * 成功删除的ID列表
     * 使用Serializable适配所有ID类型（Long/Integer/String/UUID等）
     */
    private List<Serializable> successIds;

    /**
     * 删除失败的ID及原因
     * key：失败的ID（Serializable类型）
     * value：失败原因（如"数据不存在"、"关联业务数据禁止删除"、"无操作权限"等）
     */
    private Map<Serializable, String> failedIds;

    /**
     * 便捷方法：自动计算成功/失败数量 + 总请求数（若未手动设置）
     * 业务模块只需设置successIds/failedIds，调用此方法即可自动填充数量字段，减少重复代码
     */
    public void calculateCount() {
        // 计算成功数量
        if (this.successIds != null && !this.successIds.isEmpty()) {
            this.successCount = this.successIds.size();
        }

        // 计算失败数量
        if (this.failedIds != null && !this.failedIds.isEmpty()) {
            this.failedCount = this.failedIds.size();
        }

        // 自动填充总请求数（若前端传了totalRequest则保留，否则自动计算）
        if (this.totalRequest == null) {
            this.totalRequest = this.successCount + this.failedCount;
        }
    }
}
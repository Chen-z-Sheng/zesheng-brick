package com.zesheng.sys.service.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("demoJobHandler")
public class DemoJobHandler implements JobHandler {

    @Override
    public String execute(String handlerParam) {
        // 示例执行器：用于验证定时任务链路是否通畅
        String message = "演示任务执行成功，参数：" + (handlerParam == null ? "" : handlerParam);
        log.info(message);
        return message;
    }
}

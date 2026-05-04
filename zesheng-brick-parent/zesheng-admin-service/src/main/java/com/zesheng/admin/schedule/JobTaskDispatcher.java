package com.zesheng.admin.schedule;

import com.zesheng.sys.service.IJobTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobTaskDispatcher {

    private final IJobTaskService jobTaskService;

    @Scheduled(fixedDelayString = "${job.dispatcher.fixed-delay-ms:30000}")
    public void dispatch() {
        try {
            jobTaskService.scanAndExecuteDueTasks();
        } catch (Exception ex) {
            log.error("扫描定时任务失败", ex);
        }
    }
}

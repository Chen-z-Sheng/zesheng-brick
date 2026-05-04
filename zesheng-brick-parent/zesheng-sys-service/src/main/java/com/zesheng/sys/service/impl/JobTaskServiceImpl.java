package com.zesheng.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zesheng.common.request.PageAndSortQueryRequest;
import com.zesheng.common.response.R;
import com.zesheng.sys.entity.JobTask;
import com.zesheng.sys.entity.JobTaskLog;
import com.zesheng.sys.mapper.JobTaskLogMapper;
import com.zesheng.sys.mapper.JobTaskMapper;
import com.zesheng.sys.model.request.JobTaskSaveRequest;
import com.zesheng.sys.model.request.JobTaskUpdateRequest;
import com.zesheng.sys.service.IJobTaskService;
import com.zesheng.sys.service.job.JobHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class JobTaskServiceImpl implements IJobTaskService {

    private static final int ENABLED = 1;
    private static final int DISABLED = 0;
    private static final int EXECUTE_SUCCESS = 1;
    private static final int EXECUTE_FAIL = 0;

    @Resource
    private JobTaskMapper jobTaskMapper;
    @Resource
    private JobTaskLogMapper jobTaskLogMapper;
    @Resource
    private Map<String, JobHandler> jobHandlerMap = Collections.emptyMap();

    @Override
    public List<JobTask> list() {
        return jobTaskMapper.selectList(new LambdaQueryWrapper<JobTask>().orderByAsc(JobTask::getId));
    }

    @Override
    public IPage<JobTask> page(PageAndSortQueryRequest queryRequest) {
        Page<JobTask> page = new Page<>(queryRequest.getPageNum(), queryRequest.getPageSize());
        LambdaQueryWrapper<JobTask> wrapper = new LambdaQueryWrapper<JobTask>().orderByAsc(JobTask::getId);
        return jobTaskMapper.selectPage(page, wrapper);
    }

    @Override
    public JobTask getById(Long id) {
        return jobTaskMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<JobTask> save(JobTaskSaveRequest request) {
        validateCronExpression(request.getCronExpression());
        JobTask task = new JobTask();
        task.setJobName(request.getJobName().trim());
        task.setCronExpression(request.getCronExpression().trim());
        task.setHandlerName(request.getHandlerName().trim());
        task.setHandlerParam(request.getHandlerParam());
        task.setStatus(normalizeStatus(request.getStatus()));
        task.setRunning(0);
        task.setLastExecuteStatus(DISABLED);
        task.setLastExecuteMessage("未执行");
        task.setNextExecuteAt(calculateNextExecuteTime(task.getCronExpression(), LocalDateTime.now()));
        jobTaskMapper.insert(task);
        return R.success(jobTaskMapper.selectById(task.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<JobTask> update(Long id, JobTaskUpdateRequest request) {
        JobTask task = jobTaskMapper.selectById(id);
        if (task == null) {
            return R.error("定时任务不存在");
        }
        validateCronExpression(request.getCronExpression());
        task.setJobName(request.getJobName().trim());
        task.setCronExpression(request.getCronExpression().trim());
        task.setHandlerName(request.getHandlerName().trim());
        task.setHandlerParam(request.getHandlerParam());
        task.setStatus(normalizeStatus(request.getStatus()));
        task.setNextExecuteAt(calculateNextExecuteTime(task.getCronExpression(), LocalDateTime.now()));
        jobTaskMapper.updateById(task);
        return R.success(jobTaskMapper.selectById(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Integer> deleteById(Long id) {
        int rows = jobTaskMapper.deleteById(id);
        return R.success(rows);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void scanAndExecuteDueTasks() {
        LocalDateTime now = LocalDateTime.now();
        List<JobTask> dueTasks = jobTaskMapper.selectList(
                new LambdaQueryWrapper<JobTask>()
                        .eq(JobTask::getStatus, ENABLED)
                        .eq(JobTask::getRunning, 0)
                        .isNotNull(JobTask::getNextExecuteAt)
                        .le(JobTask::getNextExecuteAt, now)
                        .orderByAsc(JobTask::getNextExecuteAt)
                        .last("limit 20")
        );
        for (JobTask task : dueTasks) {
            executeTask(task, false);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<String> triggerNow(Long id) {
        JobTask task = jobTaskMapper.selectById(id);
        if (task == null) {
            return R.error("定时任务不存在");
        }
        executeTask(task, true);
        return R.success("触发成功");
    }

    private void executeTask(JobTask task, boolean manualTrigger) {
        int claimRows = jobTaskMapper.update(
                null,
                new LambdaUpdateWrapper<JobTask>()
                        .set(JobTask::getRunning, 1)
                        .eq(JobTask::getId, task.getId())
                        .eq(JobTask::getRunning, 0)
        );
        if (claimRows <= 0) {
            return;
        }

        LocalDateTime startTime = LocalDateTime.now();
        JobTaskLog taskLog = new JobTaskLog();
        taskLog.setTaskId(task.getId());
        taskLog.setJobName(task.getJobName());
        taskLog.setStartTime(startTime);

        Integer executeStatus = EXECUTE_SUCCESS;
        String executeMessage = "执行成功";
        String errorStack = null;
        try {
            JobHandler handler = jobHandlerMap.get(task.getHandlerName());
            if (handler == null) {
                throw new IllegalArgumentException("找不到执行器: " + task.getHandlerName());
            }
            String handlerResult = handler.execute(task.getHandlerParam());
            if (handlerResult != null && !handlerResult.isBlank()) {
                executeMessage = handlerResult;
            }
        } catch (Exception ex) {
            executeStatus = EXECUTE_FAIL;
            executeMessage = ex.getMessage() == null ? "执行失败" : ex.getMessage();
            errorStack = getStackTrace(ex);
            log.error("执行定时任务失败，taskId={}", task.getId(), ex);
        } finally {
            LocalDateTime endTime = LocalDateTime.now();
            taskLog.setEndTime(endTime);
            taskLog.setStatus(executeStatus);
            taskLog.setMessage(executeMessage);
            taskLog.setErrorStack(errorStack);
            jobTaskLogMapper.insert(taskLog);

            LambdaUpdateWrapper<JobTask> updateWrapper = new LambdaUpdateWrapper<JobTask>()
                    .set(JobTask::getRunning, 0)
                    .set(JobTask::getLastExecuteAt, startTime)
                    .set(JobTask::getLastExecuteStatus, executeStatus)
                    .set(JobTask::getLastExecuteMessage, truncateMessage(executeMessage))
                    .eq(JobTask::getId, task.getId());
            if (!manualTrigger) {
                updateWrapper.set(JobTask::getNextExecuteAt, calculateNextExecuteTime(task.getCronExpression(), startTime));
            }
            jobTaskMapper.update(null, updateWrapper);
        }
    }

    private Integer normalizeStatus(Integer status) {
        return Integer.valueOf(ENABLED).equals(status) ? ENABLED : DISABLED;
    }

    private void validateCronExpression(String cronExpression) {
        if (cronExpression == null || cronExpression.isBlank()) {
            throw new IllegalArgumentException("Cron表达式不能为空");
        }
        CronExpression.parse(cronExpression.trim());
    }

    private LocalDateTime calculateNextExecuteTime(String cronExpression, LocalDateTime baseTime) {
        CronExpression expression = CronExpression.parse(cronExpression);
        LocalDateTime nextExecuteAt = expression.next(baseTime);
        if (nextExecuteAt == null) {
            throw new IllegalArgumentException("Cron表达式未计算出下次执行时间");
        }
        return nextExecuteAt;
    }

    private String truncateMessage(String message) {
        if (message == null) {
            return null;
        }
        return message.length() > 500 ? message.substring(0, 500) : message;
    }

    private String getStackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }
}

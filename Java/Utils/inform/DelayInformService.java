package com.weimob.comb.announcement.server.service.inform;

import com.alibaba.fastjson.JSONObject;
import com.weimob.comb.common.constants.enums.BizEnum;
import com.weimob.comb.common.utils.CollectionUtil;
import com.weimob.comb.common.utils.DateUtils;
import com.weimob.comb.common.utils.IpUtils;
import com.weimob.comb.user.server.service.RedisService;
import com.weimob.comb.user.server.service.inform.DelayInformTask;
import com.weimob.comb.user.server.service.inform.InformBo;
import com.weimob.comb.user.server.service.inform.InformService;
import com.weimob.comb.user.server.service.inform.InformTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author qianliao.zhuang
 */
@Service
public class DelayInformService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DelayInformService.class);

    @Value("${comb.job.machine}")
    private String jobMachineIp;

    @Autowired
    private RedisService redisService;
    @Autowired
    private InformService informService;

    @Scheduled(cron = "0 */5 * * * *")
    public void doJob() {
        if (!IpUtils.isMatchLocalIp(jobMachineIp)) {
            LOGGER.info("job ip: {} is not matching local machine ip: {}",
                    jobMachineIp, IpUtils.getLocalIp());
            return;
        }

        List<DelayInformTask> tasks = redisService.getAllDelayInformTask(
                BizEnum.CLASS_MASTER.getBizName(), DateUtils.now());
        if (CollectionUtil.isEmpty(tasks)) {
            LOGGER.info("There are currently no executable tasks!");
            return;
        }
        LOGGER.info("Let's executing tasks!");
        executeTask(tasks);
    }

    private void executeTask(List<DelayInformTask> tasks) {
        for (DelayInformTask task : tasks) {
            try {
                doExecuteTask(task);
                LOGGER.info("executing task: {} success!", JSONObject.toJSONString(task));
            } catch (Exception e) {
                LOGGER.error("executing task: {} failed!",
                        JSONObject.toJSONString(task), e);
            }
        }
    }

    private void doExecuteTask(DelayInformTask task) {
        InformTypeEnum informType = InformTypeEnum.valueOfType(task.getInformType());
        InformBo inform = from(task);
        informService.sendInformNow(inform, informType);
    }

    private InformBo from(DelayInformTask task) {
        InformTypeEnum informType = InformTypeEnum.valueOfType(task.getInformType());
        Class<? extends InformBo> inform = InformTypeEnum.getInformBo(informType);
        String informName = InformBo.getShortInformName(inform);
        String content = JSONObject.toJSONString(task.getParams().get(informName));
        return JSONObject.parseObject(content, inform);
    }
}

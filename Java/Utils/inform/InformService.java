package com.weimob.comb.user.server.service.inform;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.weimob.comb.common.utils.DateUtils;
import com.weimob.comb.user.server.service.RedisService;
import com.weimob.comb.user.server.service.ThreadPoolSingleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author qianliao.zhuang
 */
@Service
public class InformService implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(InformService.class);
    private static final Map<String, AbstractInformService> SERVICE_MAP = Maps.newConcurrentMap();
    private static final ThreadPoolTaskExecutor EXECUTORS = ThreadPoolSingleton.THREADPOOL.getThreadPool();

    @Autowired
    private FormInformService formInformService;
    @Autowired
    private GroupTransferInformService groupTransferInformService;
    @Autowired
    private List<AbstractInformService> informServices;
    @Autowired
    private RedisService redisService;

    public static void registerInformService(InformTypeEnum informType, AbstractInformService informService) {
        SERVICE_MAP.put(informType.getType(), informService);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        for (AbstractInformService service : informServices) {
            if (service.getInformType() != null &&
                    service.getInformBo() != null) {
                InformTypeEnum.registerInformBo(service.getInformType(), service.getInformBo());
                registerInformService(service.getInformType(), service);
            }
        }
    }

    public boolean sendTransfer(Long groupId, String transferTo) {
        checkNotNull(groupId, "groupId must be not null");
        checkNotNull(transferTo, "target openId must be not null");

        Map<String, Object> params = Maps.newHashMapWithExpectedSize(4);
        params.put("groupId", groupId);
        params.put("transferTo", transferTo);
        groupTransferInformService.setParams(params);
        return groupTransferInformService.send();
    }

    /**
     * 保存FormId
     *
     * @param openId
     * @param formId
     * @return
     */
    public boolean exportFormId(String bizType, String openId, String formId) {
        checkNotNull(bizType, "bizType must be not null");
        checkNotNull(openId, "openId must be not null");
        checkNotNull(formId, "formId must be not null");

        Map<String, Object> params = Maps.newHashMapWithExpectedSize(4);
        params.put("bizType", bizType);
        params.put("openId", openId);
        params.put("formId", formId);
        formInformService.setParams(params);
        return formInformService.send();
    }

    /**
     * 异步发送通知
     * @param sender：发送者
     * @param informType：通知类型
     * @param o：通知内容
     * @param extend：扩展字段
     * @return
     */
    public boolean sendInformAsyn(final Long sender, final InformTypeEnum informType,
                                  final Object o, final Map<String, Object> extend) {
        LOGGER.info("send inform with asyn");
        EXECUTORS.execute(new Runnable() {
            @Override
            public void run() {
                sendInform(sender, informType, o, extend);
            }
        });
        return true;
    }

    /**
     * 发送通知
     * @param sender：发送者
     * @param informType：通知类型
     * @param o：通知内容
     * @param extend：扩展字段
     * @return
     */
    public boolean sendInform(final Long sender, final InformTypeEnum informType,
                              final Object o, final Map<String, Object> extend) {
        checkNotNull(sender, "sender must be not null");
        checkNotNull(informType, "informType must be not null");
        checkNotNull(o, "Object must be not null");
        checkNotNull(InformTypeEnum.getInformBo(informType), "InformBo must be not null");

        Map<String, Object> extendMap = extend == null ?
                new HashMap<String, Object>(2) : extend;
        extendMap.put("sender", sender);

        InformBo inform = InformBo.from(o, InformTypeEnum.getInformBo(informType), informType, extendMap);

        LOGGER.info("sender: {} is sending inform: {}. Inform Content: {}. Extend: {}",
                sender, informType.getName(),
                JSONObject.toJSONString(o), JSONObject.toJSONString(extendMap));

        if (inform == null) {
            LOGGER.info("inform is null. Do nothing just return");
            return true;
        }

        AbstractInformService informService = SERVICE_MAP.get(informType.getType());
        if (informService == null) {
            LOGGER.warn("There is no suitable inform service for type: {}", informType.getType());
            throw new UnsupportedOperationException(
                    String.format("There is no suitable inform service for type: %s", informType.getType())
            );
        }

        if (!informService.isDelay()) {
            LOGGER.info("No delay task. Send inform right now");
            return sendInformNow(inform, informType);
        }

        LOGGER.info("send delay task to redis");
        Date delayTo = getDelay(inform, informType);
        DelayInformTask task = inform.buildDelayTask(delayTo).build();
        if (DateUtils.compare(delayTo, DateUtils.now()) > 0) {
            redisService.pushDelayInformTask(task);
            LOGGER.info("send delay task to redis done");
        } else {
            redisService.removeDelayInformTask(task);
            LOGGER.info("remove delay task because delay: {} less than now", delayTo);
        }
        return true;
    }

    public boolean sendInformNow(InformBo inform, InformTypeEnum informType) {
        AbstractInformService informService = SERVICE_MAP.get(informType.getType());
        if (informService == null) {
            LOGGER.warn("There is no suitable inform service for type: {}", informType.getType());
            return true;
        }
        informService.setParams(inform.buildInformContent());
        informService.send();
        return true;
    }

    private Date getDelay(InformBo inform, InformTypeEnum informType) {
        AbstractInformService informService = SERVICE_MAP.get(informType.getType());
        Date delayTo = informService.getDelayTo(inform);
        if (delayTo == null) {
            LOGGER.warn("delayTo is null. please check!");
            throw new IllegalArgumentException("delayTo is null. please check!");
        }
        return delayTo;
    }
}

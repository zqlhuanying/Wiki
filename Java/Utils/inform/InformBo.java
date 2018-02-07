package com.weimob.comb.user.server.service.inform;

import com.google.common.collect.Maps;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ClassUtils;

import java.beans.Introspector;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * @author qianliao.zhuang
 */
@Data
public class InformBo implements Serializable {
    private static final Logger LOGGER = LoggerFactory.getLogger(InformBo.class);

    private Long informId;
    private InformTypeEnum informType;
    private String bizType;
    private Long groupId;
    private String groupName;
    private Long sender;

    public static String getShortInformName(Class<?> clazz) {
        String shortName = ClassUtils.getShortName(clazz);
        return Introspector.decapitalize(shortName);
    }

    /**
     * 将对象 o 转换成对应的 InformBo
     * @param o：对象
     * @param inform：通知Bo
     * @param informType：通知类型
     * @param extend：扩展字段，用于对象 o 中不存在的字段，但是在构建 InformBo 又必须的字段
     * @return
     */
    public static InformBo from(Object o, Class<? extends InformBo> inform,
                                InformTypeEnum informType, Map<String, Object> extend) {
        try {
            return inform.newInstance().of(o, informType, extend);
        } catch (InstantiationException | IllegalAccessException e) {
            LOGGER.error("can't instant class: {}", inform, e);
            return null;
        }
    }

    public DelayInformTask.Builder buildDelayTask(Date delayTo) {
        return DelayInformTask.create()
                .setInformId(informId)
                .setInformType(informType.getType())
                .setBizType(bizType)
                .setGroupId(groupId)
                .setSender(sender)
                .setDelayTo(delayTo)
                .setParams(buildInformContent());
    }

    public Map<String, Object> buildInformContent() {
        Map<String, Object> params = Maps.newHashMapWithExpectedSize(4);
        params.put(getShortInformName(this.getClass()), this);
        return params;
    }

    /**
     * 根据传进来的对象，生成各自InformBo, 具体实现参见子类
     * @param o
     * @return
     */
    protected InformBo of(Object o, InformTypeEnum informType, Map<String, Object> extend) {
        throw new UnsupportedOperationException();
    }

    protected Object getValueFromExtendMap(Map<String, Object> extend, String key) {
        return extend == null ? null : extend.get(key);
    }
}

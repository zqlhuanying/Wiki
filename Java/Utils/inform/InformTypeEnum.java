package com.weimob.comb.user.server.service.inform;

import com.google.common.collect.Maps;
import lombok.Getter;

import java.util.Map;

/**
 * @author qianliao.zhuang
 */
public enum InformTypeEnum {
    /**
     * 公告发布通知
     */
    ANNOUNCEMENT_ASSIGN("1.1", "公告发布"),
    /**
     * 作业发布通知
     */
    HOMEWORK_ASSIGN("2.1", "作业发布"),
    /**
     * 作业结果通知
     */
    HOMEWORK_RESULT("2.2", "作业结果"),
    /**
     * 作业未完成通知
     */
    HOMEWORK_UNDO("2.3", "作业未完成"),
    /**
     * 报名发布通知
     */
    ACTIVITY_ASSIGN("4.1", "报名发布"),
    /**
     * 报名截止通知
     */
    ACTIVITY_DEADLINE("4.2", "报名截止"),
    /**
     * 报名即将截止
     */
    ACTIVITY_BEFORE_DEADLINE("4.3", "报名即将截止"),
    /**
     * 活动开始前通知（活动开始前一天20：00）
     * 需要特殊处理
     * getDelay(): 返回的是相对于活动当天凌晨时间的偏移量
     */
    ACTIVITY_BEFORE_START("4.4", "活动开始"),
    ACTIVITY_SUCCESS("4.5", "报名成功");

    private static final Map<String, Class<? extends InformBo>> INFORMBO_MAP = Maps.newConcurrentMap();

    @Getter
    private String type;
    @Getter
    private String name;

    InformTypeEnum(String type, String name) {
        this.type = type;
        this.name = name;
    }

    public static Class<? extends InformBo> getInformBo(InformTypeEnum informType) {
        return INFORMBO_MAP.get(informType.getType());
    }

    public static void registerInformBo(InformTypeEnum informType, Class<? extends InformBo> inform) {
        INFORMBO_MAP.put(informType.getType(), inform);
    }

    public static InformTypeEnum valueOfType(String informType) {
        InformTypeEnum[] values = InformTypeEnum.values();
        for (InformTypeEnum value : values) {
            if (value.getType().equals(informType)) {
                return value;
            }
        }
        throw new UnsupportedOperationException(
                String.format("can't find suitable informTypeEnum for type: %s", informType)
        );
    }
}

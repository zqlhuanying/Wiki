package com.weimob.comb.user.server.service.inform;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author qianliao.zhuang
 */
@Getter
public class DelayInformTask implements Serializable {
    /**
     * 作业通知：作业ID
     * 公告通知：公告ID
     * 报名通知：报名ID
     */
    private Long informId;
    /**
     * 通知类型
     */
    private String informType;
    private String bizType;
    private Long groupId;
    private Long sender;
    /**
     * 延迟到该时间点执行
     */
    private Date delayTo;
    /**
     * 通知内容
     */
    private Map<String, Object> params;

    private DelayInformTask(Builder builder) {
        this.informId = builder.informId;
        this.informType = builder.informType;
        this.bizType = builder.bizType;
        this.groupId = builder.groupId;
        this.sender = builder.sender;
        this.delayTo = builder.delayTo;
        this.params = builder.params;
        checkNotNull(informId, "informId must be not null");
        checkNotNull(informType, "informType must be not null");
        checkNotNull(bizType, "bizType must be not null");
        checkNotNull(groupId, "groupId must be not null");
        checkNotNull(sender, "sender must be not null");
        checkNotNull(delayTo, "delayTo must be not null");
    }

    public static Builder create() {
        return new Builder();
    }

    @Setter
    @Accessors(chain = true)
    public static class Builder {
        private Long informId;
        private String informType;
        private String bizType;
        private Long groupId;
        private Long sender;
        private Date delayTo;
        private Map<String, Object> params;

        public DelayInformTask build() {
            return new DelayInformTask(this);
        }
    }
}

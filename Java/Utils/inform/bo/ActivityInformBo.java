package com.weimob.comb.announcement.server.service.inform.bo;

import com.weimob.comb.announcement.service.bo.ActivityBo;
import com.weimob.comb.common.utils.CastUtil;
import com.weimob.comb.user.server.service.inform.InformBo;
import com.weimob.comb.user.server.service.inform.InformTypeEnum;
import lombok.Data;

import java.util.Date;
import java.util.Map;

/**
 * @author qianliao.zhuang
 */
@Data
public class ActivityInformBo extends InformBo {

    private String title;
    private Date withdrawTime;
    private String assigner;
    /**
     * 多少人已报名
     */
    private Integer done;

    /**
     * 活动开始时间
     */
    private Date startTime;
    /**
     * 活动地点
     */
    private String address;

    /**
     * 报名成功的用户ID
     */
    private Long joinerTo;

    @Override
    protected InformBo of(Object o, InformTypeEnum informType, Map<String, Object> extend) {
        if (o instanceof ActivityBo) {
            ActivityBo activity = (ActivityBo) o;
            ActivityInformBo activityInform = CastUtil.convert(ActivityBo.class, ActivityInformBo.class, activity);
            activityInform.setInformId(activity.getActivityId());
            activityInform.setInformType(informType);
            activityInform.setSender((Long) getValueFromExtendMap(extend, "sender"));
            if (informType == InformTypeEnum.ACTIVITY_SUCCESS) {
                activityInform.setJoinerTo((Long) getValueFromExtendMap(extend, "joinerTo"));
            }
            return activityInform;
        }
        return null;
    }
}

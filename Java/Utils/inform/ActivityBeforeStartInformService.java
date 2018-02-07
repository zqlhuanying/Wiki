package com.weimob.comb.announcement.server.service.inform;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.weimob.comb.announcement.server.service.ActivityServiceImpl;
import com.weimob.comb.announcement.server.service.inform.bo.ActivityInformBo;
import com.weimob.comb.announcement.service.bo.ActivityReadBo;
import com.weimob.comb.announcement.service.bo.SignUpListBo;
import com.weimob.comb.common.utils.CollectionUtil;
import com.weimob.comb.common.utils.DateUtils;
import com.weimob.comb.user.server.service.GroupServiceImpl;
import com.weimob.comb.user.server.service.dao.model.user.GroupUser;
import com.weimob.comb.user.server.service.inform.ClassMasterInformService;
import com.weimob.comb.user.server.service.inform.InformBo;
import com.weimob.comb.user.server.service.inform.InformBuilder;
import com.weimob.comb.user.server.service.inform.InformTypeEnum;
import com.weimob.comb.user.service.bo.UserGroupBo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author qianliao.zhuang
 * 报名活动开始前一天晚上20:00通知
 * 延迟通知
 * 通知到所有报名的成员和老师
 */
@Service
public class ActivityBeforeStartInformService extends ClassMasterInformService {

    private final String informName = InformBo.getShortInformName(getInformBo());

    private ActivityInformBo activityInform;
    private List<String> targets;

    @Autowired
    private GroupServiceImpl groupService;
    @Autowired
    private ActivityServiceImpl activityService;

    @Override
    public void initParams() {
        this.activityInform = JSONObject.parseObject(JSONObject.toJSONString(params.get(informName)), ActivityInformBo.class);
        init();
    }

    @Override
    public Serializable buildBatchRequest() {
        return InformBuilder.create()
                .setSystemName(config.getSystemName())
                .setAppId(config.getAppId())
                .setMessageType(config.getMessageType())
                .setTemplateId(config.getActivityBeforeStartTemplate())
                .setBizType(getBizType())
                .setRedirectUrl(buildRedirectUrl())
                .setOpenIds(targets)
                .buildWechatBatchRequest(buildContent());
    }

    @Override
    public boolean isSendBatch() {
        return true;
    }

    @Override
    public boolean isDelay() {
        return true;
    }

    @Override
    public Date getDelayTo(InformBo o) {
        if (o instanceof ActivityInformBo) {
            Date delayTo = ((ActivityInformBo) o).getStartTime();
            delayTo = DateUtils.getBeginOfTheDay(delayTo);
            return DateUtils.minusHours(delayTo, 4);
        }
        return null;
    }

    @Override
    public InformTypeEnum getInformType() {
        return InformTypeEnum.ACTIVITY_BEFORE_START;
    }

    @Override
    public Class<? extends InformBo> getInformBo() {
        return ActivityInformBo.class;
    }

    @Override
    protected Set<Long> getNeededSender() {
        Set<Long> neededSender = Sets.newHashSetWithExpectedSize(32);
        List<GroupUser> admins = groupService.getGroupAdmins(activityInform.getGroupId(), null);
        SignUpListBo joiners = activityService.getSignUpList(activityInform.getInformId());
        for (GroupUser admin : CollectionUtil.safeNull(admins)) {
            neededSender.add(admin.getUid());
        }
        for (ActivityReadBo joiner : CollectionUtil.safeNull(joiners.getSignUpReads())) {
            neededSender.add(joiner.getUid());
        }
        return neededSender;
    }

    private String buildRedirectUrl() {
        return String.format(config.getActivityBeforeStartUrl(),
                activityInform.getGroupId(), activityInform.getInformId());
    }

    private Map<String, Object> buildContent() {
        String title = activityInform.getTitle();
        String startTime = DateUtils.parse(activityInform.getStartTime(), "yyyy年MM月dd日");
        String address = activityInform.getAddress();
        String groupName = activityInform.getGroupName();
        String assigner = activityInform.getAssigner();
        List<String> params = Lists.newArrayList(
                title, startTime,
                address, groupName, assigner
        );
        return InformBuilder.buildWechatContent(params);
    }

    private String getBizType() {
        return activityInform.getBizType();
    }

    private void init() {
        checkNotNull(activityInform, "activity inform must be not null");
        checkNotNull(activityInform.getGroupId(), "groupId must be not null");
        checkNotNull(activityInform.getBizType(), "bizType must be not null");

        UserGroupBo group = groupService.getGroup(activityInform.getGroupId(), null);
        checkNotNull(group, "groupId: {} is invalid", activityInform.getGroupId());
        activityInform.setGroupName(group.getGroupName());

        targets = getTargets(activityInform.getGroupId());
    }
}

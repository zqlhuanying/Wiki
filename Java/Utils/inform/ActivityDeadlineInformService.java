package com.weimob.comb.announcement.server.service.inform;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.weimob.comb.announcement.server.service.ActivityServiceImpl;
import com.weimob.comb.announcement.server.service.inform.bo.ActivityInformBo;
import com.weimob.comb.announcement.service.bo.SignUpListBo;
import com.weimob.comb.common.utils.CollectionUtil;
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
 * 报名截止通知
 * 延迟通知
 * 通知到老师
 */
@Service
public class ActivityDeadlineInformService extends ClassMasterInformService {

    private static final String ACTIVITY_RESULT = "%s人已报名";
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
                .setTemplateId(config.getActivityDeadlineTemplate())
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
            return ((ActivityInformBo) o).getWithdrawTime();
        }
        return null;
    }

    @Override
    public InformTypeEnum getInformType() {
        return InformTypeEnum.ACTIVITY_DEADLINE;
    }

    @Override
    public Class<? extends InformBo> getInformBo() {
        return ActivityInformBo.class;
    }

    @Override
    protected Set<Long> getNeededSender() {
        Set<Long> needSender = Sets.newHashSetWithExpectedSize(4);
        List<GroupUser> admins = groupService.getGroupAdmins(activityInform.getGroupId(), null);
        for (GroupUser admin : CollectionUtil.safeNull(admins)) {
            needSender.add(admin.getUid());
        }
        return needSender;
    }

    private String buildRedirectUrl() {
        return String.format(config.getActivityDeadlineUrl(),
                activityInform.getGroupId(), activityInform.getInformId());
    }

    private Map<String, Object> buildContent() {
        String title = activityInform.getTitle();
        String groupName = activityInform.getGroupName();
        String assigner = activityInform.getAssigner();
        int joiners = activityInform.getDone();
        List<String> params = Lists.newArrayList(
                title, groupName,
                assigner, String.format(ACTIVITY_RESULT, joiners)
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

        SignUpListBo joiners0 = activityService.getSignUpList(activityInform.getInformId());
        int joiners = joiners0.getSignUpNum() != null ? joiners0.getSignUpNum() : 0;
        activityInform.setDone(joiners);
    }
}

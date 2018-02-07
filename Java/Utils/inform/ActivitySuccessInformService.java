package com.weimob.comb.announcement.server.service.inform;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.weimob.comb.announcement.server.service.inform.bo.ActivityInformBo;
import com.weimob.comb.user.server.service.GroupServiceImpl;
import com.weimob.comb.user.server.service.inform.ClassMasterInformService;
import com.weimob.comb.user.server.service.inform.InformBo;
import com.weimob.comb.user.server.service.inform.InformBuilder;
import com.weimob.comb.user.server.service.inform.InformTypeEnum;
import com.weimob.comb.user.service.bo.UserGroupBo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author qianliao.zhuang
 * 报名成功通知
 * 通知到该成员
 */
@Service
public class ActivitySuccessInformService extends ClassMasterInformService {

    private final String informName = InformBo.getShortInformName(getInformBo());

    private ActivityInformBo activityInform;
    private List<String> targets;

    @Autowired
    private GroupServiceImpl groupService;

    @Override
    public void initParams() {
        this.activityInform = (ActivityInformBo) params.get(informName);
        init();
    }

    @Override
    public Serializable buildRequest() {
        return InformBuilder.create()
                .setSystemName(config.getSystemName())
                .setAppId(config.getAppId())
                .setMessageType(config.getMessageType())
                .setTemplateId(config.getActivitySuccessTemplate())
                .setBizType(getBizType())
                .setRedirectUrl(buildRedirectUrl())
                .setOpenIds(targets)
                .buildWechatRequest(buildContent());
    }

    @Override
    protected Set<Long> getNeededSender() {
        return Sets.newHashSet(activityInform.getJoinerTo());
    }

    @Override
    public InformTypeEnum getInformType() {
        return InformTypeEnum.ACTIVITY_SUCCESS;
    }

    @Override
    public Class<? extends InformBo> getInformBo() {
        return ActivityInformBo.class;
    }

    private String buildRedirectUrl() {
        return String.format(config.getActivitySuccessUrl(),
                activityInform.getGroupId(), activityInform.getInformId());
    }

    private Map<String, Object> buildContent() {
        String title = activityInform.getTitle();
        String groupName = activityInform.getGroupName();
        String assigner = activityInform.getAssigner();
        List<String> params = Lists.newArrayList(
                title, groupName,
                assigner, "报名成功"
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

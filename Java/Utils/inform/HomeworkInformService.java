package com.weimob.comb.announcement.server.service.inform;

import com.google.common.collect.Lists;
import com.weimob.comb.announcement.server.service.inform.bo.HomeworkInformBo;
import com.weimob.comb.user.server.service.inform.InformBo;
import com.weimob.comb.user.server.service.inform.InformTypeEnum;
import com.weimob.comb.common.utils.DateUtils;
import com.weimob.comb.user.server.service.GroupServiceImpl;
import com.weimob.comb.user.server.service.inform.ClassMasterInformService;
import com.weimob.comb.user.server.service.inform.InformBuilder;
import com.weimob.comb.user.service.bo.UserGroupBo;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author qianliao.zhuang
 * 作业发布通知
 * 通知到所有群成员，包括班主任
 */
@Service
public class HomeworkInformService extends ClassMasterInformService {

    private final String informName = InformBo.getShortInformName(getInformBo());

    private HomeworkInformBo homeworkInform;

    private List<String> targets;

    @Autowired
    private GroupServiceImpl groupService;

    @Override
    public void initParams() {
        this.homeworkInform = (HomeworkInformBo) params.get(informName);
        init();
    }

    @Override
    public Serializable buildBatchRequest() {
        return InformBuilder.create()
                .setSystemName(config.getSystemName())
                .setAppId(config.getAppId())
                .setMessageType(config.getMessageType())
                .setTemplateId(config.getHomeworkTemplate())
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
    public InformTypeEnum getInformType() {
        return InformTypeEnum.HOMEWORK_ASSIGN;
    }

    @Override
    public Class<? extends InformBo> getInformBo() {
        return HomeworkInformBo.class;
    }

    private String buildRedirectUrl() {
        return String.format(config.getHomeworkUrl(),
                homeworkInform.getInformId(), homeworkInform.getGroupId());
    }

    private Map<String, Object> buildContent() {
        String groupName = homeworkInform.getGroupName();
        String assigner = homeworkInform.getAssigner();
        String publishTime = DateUtils.parse(homeworkInform.getAddTime(), "yyyy年MM月dd日");
        String title = homeworkInform.getTitle();
        String commit = BooleanUtils.toBoolean(homeworkInform.getForcePhotos()) ? "拍照上交" : "手动确认";
        String content = homeworkInform.getContent();
        List<String> params = Lists.newArrayList(
                groupName, assigner,
                publishTime, title,
                commit, content
        );
        return InformBuilder.buildWechatContent(params);
    }

    private String getBizType() {
        return homeworkInform.getBizType();
    }

    private void init() {
        checkNotNull(homeworkInform, "homework inform must be not null");
        checkNotNull(homeworkInform.getGroupId(), "groupId must be not null");
        checkNotNull(homeworkInform.getBizType(), "bizType must be not null");

        UserGroupBo group = groupService.getGroup(homeworkInform.getGroupId(), null);
        checkNotNull(group, "groupId: {} is invalid", homeworkInform.getGroupId());
        homeworkInform.setGroupName(group.getGroupName());

        targets = getTargets(homeworkInform.getGroupId());
    }
}

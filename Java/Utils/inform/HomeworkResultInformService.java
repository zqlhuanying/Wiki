package com.weimob.comb.announcement.server.service.inform;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.weimob.comb.announcement.server.service.AnnouncementServiceImpl;
import com.weimob.comb.announcement.server.service.inform.bo.HomeworkInformBo;
import com.weimob.comb.announcement.service.bo.HomeworkBo;
import com.weimob.comb.common.utils.DateUtils;
import com.weimob.comb.user.server.service.GroupServiceImpl;
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
 * 延迟通知
 * 作业结果，仅通知到班主任
 */
@Service
public class HomeworkResultInformService extends ClassMasterInformService {

    private static final String COMMIT_STATUS = "%s人已上交，%s人未上交";
    private final String informName = InformBo.getShortInformName(getInformBo());

    private HomeworkInformBo homeworkInform;
    private List<String> targets;

    @Autowired
    private GroupServiceImpl groupService;
    @Autowired
    private AnnouncementServiceImpl announcementService;

    @Override
    public void initParams() {
        this.homeworkInform = JSONObject.parseObject(JSONObject.toJSONString(params.get(informName)), HomeworkInformBo.class);
        init();
    }

    @Override
    public Serializable buildBatchRequest() {
        return InformBuilder.create()
                .setSystemName(config.getSystemName())
                .setAppId(config.getAppId())
                .setMessageType(config.getMessageType())
                .setTemplateId(config.getHomeworkResultTemplate())
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
        if (o instanceof HomeworkInformBo) {
            return ((HomeworkInformBo) o).getWithdrawTime();
        }
        return null;
    }

    @Override
    protected Set<Long> getNeededSender() {
        return Sets.newHashSet(homeworkInform.getAssignerId());
    }

    @Override
    public InformTypeEnum getInformType() {
        return InformTypeEnum.HOMEWORK_RESULT;
    }

    @Override
    public Class<? extends InformBo> getInformBo() {
        return HomeworkInformBo.class;
    }

    private String buildRedirectUrl() {
        return String.format(config.getHomeworkResultUrl(),
                homeworkInform.getInformId(), homeworkInform.getGroupId());
    }

    private Map<String, Object> buildContent() {
        String title = homeworkInform.getTitle();
        String commitStatus = String.format(COMMIT_STATUS, homeworkInform.getDone(), homeworkInform.getUndo());
        String groupName = homeworkInform.getGroupName();
        String assigner = homeworkInform.getAssigner();
        String publishTime = DateUtils.parse(homeworkInform.getAddTime(), "yyyy年MM月dd日");
        List<String> params = Lists.newArrayList(
                title, commitStatus,
                groupName, assigner,
                publishTime
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

        // init targets
        targets = getTargets(homeworkInform.getGroupId());

        // init undo and done
        HomeworkBo homework = new HomeworkBo();
        homework.setGroupId(homeworkInform.getGroupId());
        homework.setId(homeworkInform.getInformId());
        announcementService.fillUndoHomework(homework);
        announcementService.fillDoneHomework(homework);

        homeworkInform.setUndo(homework.getUndo());
        homeworkInform.setDone(homework.getDone());
    }
}

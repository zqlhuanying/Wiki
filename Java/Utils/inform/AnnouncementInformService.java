package com.weimob.comb.announcement.server.service.inform;

import com.google.common.collect.Lists;
import com.weimob.comb.announcement.server.service.inform.bo.AnnouncementInformBo;
import com.weimob.comb.common.utils.DateUtils;
import com.weimob.comb.user.server.service.inform.ClassMasterInformService;
import com.weimob.comb.user.server.service.inform.InformBo;
import com.weimob.comb.user.server.service.inform.InformBuilder;
import com.weimob.comb.user.server.service.inform.InformTypeEnum;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author qianliao.zhuang
 * 公告发布通知
 * 通知到所有群成员，包括班主任
 */
@Service
public class AnnouncementInformService extends ClassMasterInformService {

    private final String informName = InformBo.getShortInformName(getInformBo());

    private AnnouncementInformBo announcementInform;

    private List<String> targets;

    @Override
    public void initParams() {
        this.announcementInform = (AnnouncementInformBo) params.get(informName);
        init();
    }

    @Override
    public Serializable buildBatchRequest() {
        return InformBuilder.create()
                .setSystemName(config.getSystemName())
                .setAppId(config.getAppId())
                .setMessageType(config.getMessageType())
                .setTemplateId(config.getAnnouncementTemplate())
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
        return InformTypeEnum.ANNOUNCEMENT_ASSIGN;
    }

    @Override
    public Class<? extends InformBo> getInformBo() {
        return AnnouncementInformBo.class;
    }

    private String buildRedirectUrl() {
        return String.format(config.getAnnouncementUrl(),
                announcementInform.getInformId(), announcementInform.getGroupId());
    }

    private Map<String, Object> buildContent() {
        String assigner = announcementInform.getAssigner();
        String publishTime = DateUtils.parse(announcementInform.getAddTime(), "yyyy年MM月dd日");
        String content = announcementInform.getContent();
        List<String> params = Lists.newArrayList(
                assigner, publishTime, content
        );
        return InformBuilder.buildWechatContent(params);
    }

    private String getBizType() {
        return announcementInform.getBizType();
    }

    private void init() {
        checkNotNull(announcementInform, "announcement inform must be not null");
        checkNotNull(announcementInform.getGroupId(), "groupId must be not null");
        checkNotNull(announcementInform.getBizType(), "bizType must be not null");

        targets = getTargets(announcementInform.getGroupId());
    }
}

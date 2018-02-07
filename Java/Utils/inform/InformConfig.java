package com.weimob.comb.user.server.service.inform;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @author qianliao.zhuang
 */
@Data
@Service
public class InformConfig {

    public static final String FORM_TYPE = "form";

    @Value("${xcx.system.name}")
    private String systemName;
    @Value("${app.id}")
    private String appId;
    @Value("${message.type}")
    private String messageType;

    // Group Transfer Start
    @Value("${xcx.template.group.transfer}")
    private String groupTransferTemplate;
    @Value("${xcx.redirect.url.group.transfer}")
    private String groupTransferUrl;
    // Group Transfer End

    // Homework Start
    @Value("${xcx.template.publish.homework}")
    private String homeworkTemplate;
    @Value("${xcx.redirect.url.publish.homework}")
    private String homeworkUrl;

    @Value("${xcx.template.result.homework}")
    private String homeworkResultTemplate;
    @Value("${xcx.redirect.url.result.homework}")
    private String homeworkResultUrl;

    @Value("${xcx.template.undo.homework}")
    private String homeworkUndoTemplate;
    @Value("${xcx.redirect.url.undo.homework}")
    private String homeworkUndoUrl;
    // Homework End

    // Announcement Start
    @Value("${xcx.template.publish.announcement}")
    private String announcementTemplate;
    @Value("${xcx.redirect.url.publish.announcement}")
    private String announcementUrl;
    // Announcement End

    // Activity Start
    @Value("${xcx.template.publish.activity}")
    private String activityTemplate;
    @Value("${xcx.redirect.url.publish.activity}")
    private String activityUrl;

    @Value("${xcx.template.success.activity}")
    private String activitySuccessTemplate;
    @Value("${xcx.redirect.url.success.activity}")
    private String activitySuccessUrl;

    @Value("${xcx.template.publish.activity.before.deadline}")
    private String activityBeforeDeadlineTemplate;
    @Value("${xcx.redirect.url.publish.activity.before.deadline}")
    private String activityBeforeDeadlineUrl;

    @Value("${xcx.template.publish.activity.deadline}")
    private String activityDeadlineTemplate;
    @Value("${xcx.redirect.url.publish.activity.deadline}")
    private String activityDeadlineUrl;

    @Value("${xcx.template.publish.activity.before.start}")
    private String activityBeforeStartTemplate;
    @Value("${xcx.redirect.url.publish.activity.before.start}")
    private String activityBeforeStartUrl;
    // Activity End
}

package com.weimob.comb.user.server.service.inform;

import com.weimob.saas.message.wechat.dto.request.FormRequest;
import com.weimob.saas.message.wechat.dto.request.WechatRequest;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author qianliao.zhuang
 */
@Setter
@Accessors(chain = true)
public class InformBuilder {

    private String systemName;
    private String bizType;
    private String appId;
    private String messageType;
    private String redirectUrl;
    private String templateId;
    private List<String> openIds;

    private InformBuilder() {}

    public static InformBuilder create() {
        return new InformBuilder();
    }

    /**
     * 构建微信小程序通知默认的消息体
     * @param params
     * @return
     */
    public static Map<String, Object> buildWechatContent(List<String> params) {
        Map<String, Object> data = new HashMap<>(16);
        for (int i = 1, size = params.size(); i <= size; i++) {
            Map<String, String> value = new HashMap<>(2);
            value.put("value", params.get(i - 1));

            data.put("keyword" + i, value);
        }
        return data;
    }

    public FormRequest buildFormRequest(String formId) {
        checkNotNull(bizType, "bizType must be not null");
        checkNotNull(openIds, "openIds must be not null");
        checkNotNull(formId, "formId must be not null");

        FormRequest request = new FormRequest();
        request.setBizType(bizType);
        request.setOpenId(openIds.get(0));
        request.setFormId(formId);
        request.setFormType(InformConfig.FORM_TYPE);
        return request;
    }

    public WechatRequest buildWechatRequest(Map<String, Object> sendContent) {
        checkArgs();

        WechatRequest request = new WechatRequest();
        request.setSystemName(systemName);
        request.setBizType(bizType);
        request.setAppId(appId);
        request.setMessageType(messageType);
        request.setTemplateId(templateId);
        request.setData(sendContent);
        request.setOpenId(openIds.get(0));
        request.setUrl(redirectUrl);
        return request;
    }

    public WechatRequest buildWechatBatchRequest(Map<String, Object> sendContent) {
        checkArgs();

        WechatRequest request = new WechatRequest();
        request.setSystemName(systemName);
        request.setBizType(bizType);
        request.setAppId(appId);
        request.setMessageType(messageType);
        request.setTemplateId(templateId);
        request.setData(sendContent);
        request.setOpenIds(openIds);
        request.setUrl(redirectUrl);
        return request;
    }

    private void checkArgs() {
        checkNotNull(systemName, "system name must be not null");
        checkNotNull(bizType, "bizType must be not null");
        checkNotNull(appId, "app id must be not null");
        checkNotNull(messageType, "message type must be not null");
        checkNotNull(templateId, "templateId must be not null");
        checkNotNull(redirectUrl, "redirect url must be not null");
        checkNotNull(openIds, "openIds must be not null");
    }
}

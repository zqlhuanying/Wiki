package com.weimob.comb.user.server.service.inform;

import com.alibaba.fastjson.JSONObject;
import com.weimob.dubbo.generic.utils.soa.SoaUtils;
import com.weimob.saas.message.wechat.dto.request.FormRequest;
import com.weimob.saas.message.wechat.dto.request.WechatRequest;
import com.weimob.saas.message.wechat.dto.response.FormResponse;
import com.weimob.saas.message.wechat.dto.response.WechatResponse;
import com.weimob.saas.message.wechat.service.WechatExportService;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * @author qianliao.zhuang
 */
public abstract class AbstractInformService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractInformService.class);

    @Autowired
    private WechatExportService wechatService;
    /**
     * 每个通知都需要的配置信息
     */
    @Autowired
    public InformConfig config;
    /**
     * 每个通知在构建请求时，需要特定参数去初始化信息
     */
    @Setter
    @Getter
    public Map<String, Object> params;

    public AbstractInformService () {}


    public boolean send() {
        Serializable request;
        if (isSendBatch()) {
            request = buildBatchRequestInternal();
        } else {
            request = buildRequestInternal();
        }
        if (request == null) {
            LOGGER.warn("Inform request is null.Do nothing just return");
            return true;
        }
        boolean success = false;
        if (request instanceof FormRequest) {
            success = doSendForm((FormRequest) request);
        }
        if (request instanceof WechatRequest) {
            if (isSendBatch()) {
                success = doSendWechatBatch((WechatRequest) request);
            } else {
                success = doSendWechat((WechatRequest) request);
            }
        }
        return success;
    }

    private boolean doSendForm(FormRequest request) {
        LOGGER.info("saving form id. bizType: {}, openId: {}, formId: {}",
                request.getBizType(), request.getOpenId(), request.getFormId());
        FormResponse response = SoaUtils.unpack(wechatService.saveFormInfo(request));
        LOGGER.info("the response from saving form id is: {}", JSONObject.toJSONString(response));
        return response.getCode() == 0;
    }

    private boolean doSendWechat(WechatRequest request) {
        LOGGER.info("Invoke sendWechatTemplateMsg. The Params: {}", JSONObject.toJSONString(request));
        WechatResponse wechatResponse = SoaUtils.unpack(wechatService.sendWechatTemplateMsg(request));
        LOGGER.info("the response from wechat service is: {}", JSONObject.toJSONString(wechatResponse));
        return wechatResponse.getCode() == 0;
    }

    /**
     * @param request
     * @return
     */
    private boolean doSendWechatBatch(WechatRequest request) {
        LOGGER.info("Batch sending request. The Params: {}", JSONObject.toJSONString(request));
        WechatResponse wechatResponse = SoaUtils.unpack(wechatService.sendWechatTemplateMsg(request));
        LOGGER.info("The response from batch wechat service is: {}", JSONObject.toJSONString(wechatResponse));
        return wechatResponse.getCode() == 0;
    }

    /**
     * 构建通知的请求
     * @return
     */
    public Serializable buildRequest() {
        throw new UnsupportedOperationException("build request first");
    }

    /**
     * 构建批量通知的请求
     * @return
     */
    public Serializable buildBatchRequest() {
        throw new UnsupportedOperationException("build batch request first");
    }

    /**
     * 初始化构建请求时的参数信息
     */
    public void initParams() {

    }

    public boolean isSendBatch() {
        return false;
    }

    public boolean isDelay() {
        return false;
    }

    public Date getDelayTo(InformBo o) {
        throw new UnsupportedOperationException();
    }

    public InformTypeEnum getInformType() {
        return null;
    }

    public Class<? extends InformBo> getInformBo() {
        return null;
    }

    private Serializable buildRequestInternal() {
        initParams();
        return buildRequest();
    }

    private Serializable buildBatchRequestInternal() {
        initParams();
        return buildBatchRequest();
    }
}

package com.weimob.comb.user.server.service.inform;

import com.google.common.collect.Lists;
import org.springframework.stereotype.Service;

import java.io.Serializable;

/**
 * @author qianliao.zhuang
 * 保存FormId
 */
@Service
public class FormInformService extends AbstractInformService {

    private String bizType;
    private String openId;
    private String formId;

    @Override
    public void initParams() {
        this.bizType = (String) params.get("bizType");
        this.openId = (String) params.get("openId");
        this.formId = (String) params.get("formId");
    }

    @Override
    public Serializable buildRequest() {
        return InformBuilder.create()
                .setBizType(bizType)
                .setOpenIds(Lists.newArrayList(openId))
                .buildFormRequest(formId);
    }
}

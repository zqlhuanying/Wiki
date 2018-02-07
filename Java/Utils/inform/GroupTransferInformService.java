package com.weimob.comb.user.server.service.inform;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.weimob.comb.common.utils.CollectionUtil;
import com.weimob.comb.user.server.service.GroupServiceImpl;
import com.weimob.comb.user.server.service.UserServiceImpl;
import com.weimob.comb.user.server.service.dao.model.user.Group;
import com.weimob.comb.user.server.service.dao.model.user.GroupTransfer;
import com.weimob.comb.user.server.service.dao.model.user.GroupUser;
import com.weimob.comb.user.service.bo.UserBo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author qianliao.zhuang
 * 群主转让通知
 * 通知到对应的转让人
 */
@Service
public class GroupTransferInformService extends AbstractInformService {

    private static final Map<String, String> CONTENT_MAP = Maps.newHashMapWithExpectedSize(8);

    private Long groupId;
    private String transferTo;

    @Autowired
    private GroupServiceImpl groupService;
    @Autowired
    private UserServiceImpl userService;

    @Override
    public void initParams() {
        this.groupId = (Long) params.get("groupId");
        this.transferTo = (String) params.get("transferTo");
        init();
    }

    @Override
    public Serializable buildRequest() {
        return InformBuilder.create()
                .setSystemName(config.getSystemName())
                .setAppId(config.getAppId())
                .setMessageType(config.getMessageType())
                .setTemplateId(config.getGroupTransferTemplate())
                .setBizType(getBizType())
                .setRedirectUrl(buildRedirectUrl())
                .setOpenIds(Lists.newArrayList(transferTo))
                .buildWechatRequest(buildContent());
    }

    private String buildRedirectUrl() {
        String admin = CONTENT_MAP.get("adminId");
        return String.format(config.getGroupTransferUrl(), groupId, transferTo, admin);
    }

    private Map<String, Object> buildContent() {
        String groupName = CONTENT_MAP.get("groupName");
        String adminName = CONTENT_MAP.get("adminName");
        List<String> params = Lists.newArrayList(groupName, adminName);
        return InformBuilder.buildWechatContent(params);
    }

    private String getBizType() {
        return CONTENT_MAP.get("bizType");
    }

    private void init() {
        checkNotNull(groupId, "groupId must be not null");
        checkNotNull(transferTo, "transferTo must be not null");

        Group group = groupService.getUnwrappedGroup(groupId);
        checkArgument(group != null, "groupId: %s 无效", groupId);

        GroupTransfer log = groupService.getLastTransfer(groupId);
        checkArgument(log != null, "groupId: %s 没有移交的请求", groupId);

        Long uid = log.getUid();
        List<GroupUser> admins = groupService.getGroupAdmins(groupId, uid);
        checkArgument(CollectionUtil.isNotEmpty(admins),
                "admin: %s 不是 group: %s 的管理员", uid, groupId);

        List<UserBo> owner = userService.listUsers(Lists.newArrayList(uid));
        checkNotNull(owner, "查找不到 uid: %s 的信息", uid);
        checkNotNull(owner.get(0), "查找不到 uid: %s 的信息", uid);

        Long transferTo = log.getAccessUid();
        List<UserBo> targets = userService.listUsers(Lists.newArrayList(transferTo));
        checkNotNull(targets, "查找不到 target: %s 的信息", transferTo);
        checkNotNull(targets.get(0), "查找不到 target: %s 的信息", transferTo);

        CONTENT_MAP.put("groupId", String.valueOf(groupId));
        CONTENT_MAP.put("transferTo", JSONObject.toJSONString(targets.get(0)));
        CONTENT_MAP.put("groupName", group.getGroupName());
        CONTENT_MAP.put("adminId", String.valueOf(uid));
        CONTENT_MAP.put("adminName", owner.get(0).getNickName());
        CONTENT_MAP.put("bizType", group.getBizType());
    }
}

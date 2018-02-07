package com.weimob.comb.user.server.service.inform;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.weimob.comb.common.utils.CollectionUtil;
import com.weimob.comb.user.server.service.GroupServiceImpl;
import com.weimob.comb.user.service.bo.UserBo;
import com.weimob.comb.user.service.bo.UserGroupBo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author qianliao.zhuang
 */
@Service
public class ClassMasterInformService extends AbstractInformService {

    @Autowired
    private GroupServiceImpl groupService;

    protected List<String> getTargets(Long groupId) {
        List<UserBo> candidateTargets = getCandidateTargets(groupId);
        if (CollectionUtil.isEmpty(candidateTargets)) {
            return Collections.emptyList();
        }
        final Set<Long> neededSender = getNeededSender();
        Iterable<String> targets = Iterables.transform(candidateTargets, new Function<UserBo, String>() {
            @Override
            public String apply(UserBo user) {
                if (CollectionUtil.isEmpty(neededSender) ||
                        neededSender.contains(user.getUid())) {
                    return user.getOpenId();
                }
                return null;
            }
        });
        return Lists.newArrayList(CollectionUtil.nullFilter(targets));
    }

    /**
     * 真正需要发送的对象
     * 如果为空，则表示向全体成员发送
     * 否则只向特定的用户发送
     * @return
     */
    protected Set<Long> getNeededSender() {
        return null;
    }

    private List<UserBo> getCandidateTargets(Long groupId) {
        UserGroupBo group = groupService.getGroup(groupId, null);
        checkNotNull(group, "groupId: {} is invalid", groupId);
        return group.getUsers();
    }
}

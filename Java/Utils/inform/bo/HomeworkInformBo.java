package com.weimob.comb.announcement.server.service.inform.bo;

import com.weimob.comb.announcement.service.bo.AnnouncementBo;
import com.weimob.comb.common.constants.enums.AnnouncementTypeEnum;
import com.weimob.comb.common.utils.CastUtil;
import com.weimob.comb.user.server.service.inform.InformBo;
import com.weimob.comb.user.server.service.inform.InformTypeEnum;
import com.weimob.comb.user.service.bo.UserBo;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author qianliao.zhuang
 */
@Data
public class HomeworkInformBo extends InformBo {

    private String title;

    private String content;

    private Date addTime;

    private Byte forcePhotos;

    private String assigner;

    private Long assignerId;

    // delay inform start
    private Date withdrawTime;
    private Integer done;
    private Integer undo;
    private List<UserBo> undoList;
    // delay inform end

    @Override
    protected InformBo of(Object o, InformTypeEnum informType, Map<String, Object> extend) {
        if (o instanceof AnnouncementBo) {
            AnnouncementBo homework = (AnnouncementBo) o;
            if (AnnouncementTypeEnum.HOMEWORK.getType() == homework.getType()) {
                HomeworkInformBo homeworkInform = CastUtil.convert(AnnouncementBo.class, HomeworkInformBo.class, homework);
                homeworkInform.setInformId(homework.getId());
                homeworkInform.setInformType(informType);
                homeworkInform.setSender((Long) getValueFromExtendMap(extend, "sender"));
                homeworkInform.setAssignerId(homework.getUid());
                return homeworkInform;
            }
        }
        return null;
    }
}

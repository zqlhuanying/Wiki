package com.weimob.comb.announcement.server.service.inform.bo;

import com.weimob.comb.announcement.service.bo.AnnouncementBo;
import com.weimob.comb.common.constants.enums.AnnouncementTypeEnum;
import com.weimob.comb.common.utils.CastUtil;
import com.weimob.comb.user.server.service.inform.InformBo;
import com.weimob.comb.user.server.service.inform.InformTypeEnum;
import lombok.Data;

import java.util.Date;
import java.util.Map;

/**
 * @author qianliao.zhuang
 */
@Data
public class AnnouncementInformBo extends InformBo {

    private String content;

    private Date addTime;

    private String assigner;

    @Override
    protected InformBo of(Object o, InformTypeEnum informType, Map<String, Object> extend) {
        if (o instanceof AnnouncementBo) {
            AnnouncementBo announcement = (AnnouncementBo) o;
            if (AnnouncementTypeEnum.ANNOUNCEMENT.getType() == announcement.getType()) {
                AnnouncementInformBo announcementInform = CastUtil.convert(AnnouncementBo.class, AnnouncementInformBo.class, announcement);
                announcementInform.setInformId(announcement.getId());
                announcementInform.setInformType(informType);
                announcementInform.setSender((Long) getValueFromExtendMap(extend, "sender"));
                return announcementInform;
            }
        }
        return null;
    }
}

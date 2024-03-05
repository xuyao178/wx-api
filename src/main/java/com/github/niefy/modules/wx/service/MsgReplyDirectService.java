package com.github.niefy.modules.wx.service;

import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 公众号消息处理
 * 未认证情况下，只能基于当前请求直接回复消息
 */
public interface MsgReplyDirectService {

    Logger logger = LoggerFactory.getLogger(MsgReplyDirectService.class);

    /**
     * 根据规则配置通过当前请求自动回复消息
     *
     * @param appid
     * @param exactMatch 是否精确匹配
     * @param toUser 用户openid
     * @param keywords 匹配关键词
     * @return 是否已自动回复，无匹配规则则不自动回复
     */
    WxMpXmlOutMessage tryAutoReplyDirect(String appid, boolean exactMatch, String fromUser, String toUser, String keywords);

    default WxMpXmlOutMessage reply(String fromUser, String toUser, String replyType, String replyContent) {
        try {
            if (WxConsts.KefuMsgType.TEXT.equals(replyType)) {
                return this.replyText(fromUser, toUser, replyContent);
            } else if (WxConsts.KefuMsgType.IMAGE.equals(replyType)) {
                return this.replyImage(fromUser, toUser, replyContent);
            } else if (WxConsts.KefuMsgType.VOICE.equals(replyType)) {
                return this.replyVoice(fromUser, toUser, replyContent);
            } else if (WxConsts.KefuMsgType.VIDEO.equals(replyType)) {
                return this.replyVideo(fromUser, toUser, replyContent);
            } else if (WxConsts.KefuMsgType.MUSIC.equals(replyType)) {
                return this.replyMusic(fromUser, toUser, replyContent);
            } else if (WxConsts.KefuMsgType.NEWS.equals(replyType)) {
                return this.replyNews(fromUser, toUser, replyContent);
            }
        } catch (Exception e) {
            logger.error("自动回复出错：", e);
        }
        return null;
    }

    /**
     * 回复文字消息
     */
    WxMpXmlOutMessage replyText(String fromUser, String toUser, String replyContent);

    /**
     * 回复图片消息
     */
    WxMpXmlOutMessage replyImage(String fromUser, String toUser, String mediaId);

    /**
     * 回复录音消息
     */
    WxMpXmlOutMessage replyVoice(String fromUser, String toUser, String mediaId);

    /**
     * 回复视频消息
     */
    WxMpXmlOutMessage replyVideo(String fromUser, String toUser, String mediaId);

    /**
     * 回复音乐消息
     */
    WxMpXmlOutMessage replyMusic(String fromUser, String toUser, String mediaId);

    /**
     * 回复图文消息（点击跳转到外链）
     * 图文消息条数限制在1条以内
     */
    WxMpXmlOutMessage replyNews(String fromUser, String toUser, String newsInfoJson);
}

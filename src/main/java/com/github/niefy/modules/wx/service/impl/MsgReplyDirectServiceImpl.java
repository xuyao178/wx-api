package com.github.niefy.modules.wx.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.niefy.modules.wx.entity.MsgReplyRule;
import com.github.niefy.modules.wx.entity.WxMsg;
import com.github.niefy.modules.wx.service.MsgReplyDirectService;
import com.github.niefy.modules.wx.service.MsgReplyRuleService;
import com.github.niefy.modules.wx.service.WxMsgService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutNewsMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 微信公众号消息处理
 * 基于当前请求直接回复
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MsgReplyDirectServiceImpl implements MsgReplyDirectService {

    @Autowired
    MsgReplyRuleService msgReplyRuleService;
    @Autowired
    WxMpService wxMpService;
    @Value("${wx.mp.autoReplyInterval:1000}")
    Long autoReplyInterval;
    @Autowired
    WxMsgService wxMsgService;

    @Override
    public WxMpXmlOutMessage tryAutoReplyDirect(String appid, boolean exactMatch, String fromUser, String toUser,
            String keywords) {
        List<MsgReplyRule> rules = msgReplyRuleService.getMatchedRules(appid, exactMatch, keywords);
        if (rules.isEmpty()) {
            return null;
        }
        //直接回复只能回复一个消息
        MsgReplyRule msgReplyRule = rules.get(0);
        return this.reply(fromUser, toUser, msgReplyRule.getReplyType(), msgReplyRule.getReplyContent());
    }

    @Override
    public WxMpXmlOutMessage replyText(String fromUser, String toUser, String content) {
        WxMpXmlOutMessage outMessage = WxMpXmlOutMessage.TEXT().fromUser(fromUser).toUser(toUser).content(content)
                .build();

        JSONObject json = new JSONObject().fluentPut("content", content);
        wxMsgService.addWxMsg(WxMsg.buildOutMsg(WxConsts.KefuMsgType.TEXT, toUser, json));
        return outMessage;
    }

    @Override
    public WxMpXmlOutMessage replyImage(String fromUser, String toUser, String mediaId) {
        WxMpXmlOutMessage outMessage = WxMpXmlOutMessage.IMAGE().toUser(toUser).mediaId(mediaId).build();

        JSONObject json = new JSONObject().fluentPut("mediaId", mediaId);
        wxMsgService.addWxMsg(WxMsg.buildOutMsg(WxConsts.KefuMsgType.IMAGE, toUser, json));
        return outMessage;
    }

    @Override
    public WxMpXmlOutMessage replyVoice(String fromUser, String toUser, String mediaId) {
        WxMpXmlOutMessage outMessage = WxMpXmlOutMessage.VOICE().toUser(toUser).mediaId(mediaId).build();

        JSONObject json = new JSONObject().fluentPut("mediaId", mediaId);
        wxMsgService.addWxMsg(WxMsg.buildOutMsg(WxConsts.KefuMsgType.VOICE, toUser, json));
        return outMessage;
    }

    @Override
    public WxMpXmlOutMessage replyVideo(String fromUser, String toUser, String mediaId) {
        WxMpXmlOutMessage outMessage = WxMpXmlOutMessage.VIDEO().toUser(toUser).mediaId(mediaId).build();

        JSONObject json = new JSONObject().fluentPut("mediaId", mediaId);
        wxMsgService.addWxMsg(WxMsg.buildOutMsg(WxConsts.KefuMsgType.VIDEO, toUser, json));
        return outMessage;
    }

    @Override
    public WxMpXmlOutMessage replyMusic(String fromUser, String toUser, String musicInfoJson) {
        JSONObject json = JSON.parseObject(musicInfoJson);
        WxMpXmlOutMessage outMessage = WxMpXmlOutMessage.MUSIC().toUser(toUser).musicUrl(json.getString("musicurl"))
                .hqMusicUrl(json.getString("hqmusicurl")).title(json.getString("title"))
                .description(json.getString("description")).thumbMediaId(json.getString("thumb_media_id")).build();

        wxMsgService.addWxMsg(WxMsg.buildOutMsg(WxConsts.KefuMsgType.IMAGE, toUser, json));
        return outMessage;
    }

    /**
     * 发送图文消息（点击跳转到外链） 图文消息条数限制在1条以内
     *
     * @param toUser
     * @param newsInfoJson
     * @throws WxErrorException
     */
    @Override
    public WxMpXmlOutMessage replyNews(String fromUser, String toUser, String newsInfoJson) {
        WxMpXmlOutNewsMessage.Item wxArticle = JSON.parseObject(newsInfoJson, WxMpXmlOutNewsMessage.Item.class);
        List<WxMpXmlOutNewsMessage.Item> newsList = new ArrayList<WxMpXmlOutNewsMessage.Item>() {{
            add(wxArticle);
        }};
        WxMpXmlOutMessage outMessage = WxMpXmlOutMessage.NEWS().toUser(toUser).articles(newsList).build();

        wxMsgService.addWxMsg(WxMsg.buildOutMsg(WxConsts.KefuMsgType.NEWS, toUser, JSON.parseObject(newsInfoJson)));
        return outMessage;
    }
}

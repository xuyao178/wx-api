package com.github.niefy.modules.wx.handler;


import com.github.niefy.modules.wx.entity.WxAccount;
import com.github.niefy.modules.wx.entity.WxMsg;
import com.github.niefy.modules.wx.service.MsgReplyDirectService;
import com.github.niefy.modules.wx.service.MsgReplyService;
import com.github.niefy.modules.wx.service.WxAccountService;
import com.github.niefy.modules.wx.service.WxMsgService;
import java.util.Map;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import me.chanjar.weixin.mp.util.WxMpConfigStorageHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Binary Wang
 */
@Component
public class MsgHandler extends AbstractHandler {

    Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    MsgReplyService msgReplyService;

    @Autowired
    MsgReplyDirectService msgReplyDirectService;

    @Autowired
    WxMsgService wxMsgService;

    @Autowired
    WxAccountService wxAccountService;

    private static final String TRANSFER_CUSTOMER_SERVICE_KEY = "人工";

    @Override
    public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage, Map<String, Object> context, WxMpService wxMpService,
            WxSessionManager sessionManager) {

        String textContent = wxMessage.getContent();
        String fromUser = wxMessage.getFromUser();
        String toUser = wxMessage.getToUser();
        String appid = WxMpConfigStorageHolder.get();

        //未认证的账号，直接通过当前请求回复消息
        WxAccount wxAccount = wxAccountService.getById(appid);
        if (!wxAccount.isVerified()) {
            return msgReplyDirectService.tryAutoReplyDirect(appid, false, toUser, fromUser, textContent);
        }
        //认证的账号走客服消息回复
        boolean autoReplyed = msgReplyService.tryAutoReply(appid, false, fromUser, textContent);
        //当用户输入关键词如“你好”，“客服”等，并且有客服在线时，把消息转发给在线客服
        if (TRANSFER_CUSTOMER_SERVICE_KEY.equals(textContent) || !autoReplyed) {
            wxMsgService.addWxMsg(WxMsg.buildOutMsg(WxConsts.KefuMsgType.TRANSFER_CUSTOMER_SERVICE, fromUser, null));
            return WxMpXmlOutMessage.TRANSFER_CUSTOMER_SERVICE().fromUser(wxMessage.getToUser()).toUser(fromUser)
                    .build();
        }
        return null;

    }
}

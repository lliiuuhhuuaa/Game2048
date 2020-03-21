package com.sp.game2048.socket.service;

import android.os.Message;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.sp.game2048.data.SQLiteData;
import com.sp.game2048.enums.CountDownMsgTypeEnum;
import com.sp.game2048.enums.ResultCodeEnum;
import com.sp.game2048.game.service.GameService;
import com.sp.game2048.handle.HandleMessage;
import com.sp.game2048.socket.enums.MsgHandleTypeEnum;
import com.sp.game2048.user.service.UserService;
import com.sp.game2048.util.AlertUtil;
import com.sp.game2048.util.ClassUtil;
import com.sp.game2048.util.UserUtil;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;

public class SocketService {
    //socket连接
    private Socket socket;
    /**
     * @do 开始连接socket
     * @author liuhua
     * @date 2020/3/14 11:05 PM
     */
    public void connect(String type,Runnable callback){
        SQLiteData sqLiteData = ClassUtil.get(SQLiteData.class);
        String domain = sqLiteData.getConfig("domain");
        String token = UserUtil.getToken();
        try {
            URL url = new URL(domain);
            if(socket!=null){
                //先断开之前连接
                socket.disconnect();
            }
            socket = IO.socket(String.format("%s://%s?tk=%s&type=%s",url.getProtocol(),url.getHost(),token,type));
            //连接错误事件
            socket.on("connect_failed", args -> {
                if(!(args[0] instanceof Exception)){
                    JSONObject jsonObject = JSONObject.parseObject(args[0].toString());
                    Integer code = jsonObject.getInteger("code");
                    if (ResultCodeEnum.NO_AUTH.getValue().equals(code)) {
                        Message message = Message.obtain(ClassUtil.get(HandleMessage.class), CountDownMsgTypeEnum.CALL_BACK.getValue());
                        message.obj = new Object[]{UserService.class};
                        message.getData().putString("method", "unLogin");
                        ClassUtil.get(HandleMessage.class).sendMessage(message);
                    } else if (ResultCodeEnum.ERROR.getValue().equals(code)) {
                        AlertUtil.toast(jsonObject.getString("msg"), Toast.LENGTH_SHORT);
                    }
                }
            });
            //连接超时事件
            socket.on(Socket.EVENT_CONNECT_TIMEOUT, args -> {
                AlertUtil.toast("网络连接超时",Toast.LENGTH_SHORT);
            });
            //连接成功事件
            socket.on(Socket.EVENT_CONNECT, args -> {
                if(callback!=null) {
                    callback.run();
                }
            });
            socket.on(Socket.EVENT_ERROR,args -> {
                AlertUtil.toast("EVENT_ERROR",Toast.LENGTH_SHORT);
            });
            socket.on(MsgHandleTypeEnum.NOTICE_MESSAGE.getValue(), args -> {
                AlertUtil.toast(JSONObject.toJSONString(args), Toast.LENGTH_LONG);
            });
            socket.connect();
        } catch (URISyntaxException | MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * @do 发送消息
     * @author liuhua
     * @date 2020/3/15 1:06 PM
     */
    public void sendMessage(String handle, Object obj, Ack ack){
        if(socket!=null){
            socket.emit(handle,JSONObject.toJSONString(obj),ack);
        }
    }
    /**
     * @do 是否在线
     * @author liuhua
     * @date 2020/3/19 8:32 AM
     */
    public boolean isOnline(){
        return socket!=null&&socket.connected();
    }
    /**
     * @do 断开连接
     * @author liuhua
     * @date 2020/3/21 3:38 PM
     */
    public void disConnect() {
        if(socket!=null){
            //先断开之前连接
            socket.disconnect();
        }
    }
}

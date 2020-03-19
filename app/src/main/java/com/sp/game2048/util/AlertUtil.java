package com.sp.game2048.util;

import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.Toast;

import com.sp.game2048.GameActivity;
import com.sp.game2048.MainActivity;
import com.sp.game2048.enums.CountDownMsgTypeEnum;
import com.sp.game2048.handle.HandleMessage;

import cn.pedant.SweetAlert.SweetAlertDialog;
/**
 * @do 弹窗工具
 * @author liuhua
 * @date 2020/3/12 8:18 PM
 */
public class AlertUtil {
    /**
     * @do 错误弹窗
     * @author liuhua
     * @date 2020/3/12 9:19 PM
     */
    public static void alertError(String msg) {
        Message message = Message.obtain(ClassUtil.get(HandleMessage.class), CountDownMsgTypeEnum.ALERT_MSG.getValue());
        Bundle data = message.getData();
        data.putString("msg",msg);
        data.putInt("type",SweetAlertDialog.ERROR_TYPE);
        data.putString("title","错误提示");
        ClassUtil.get(HandleMessage.class).sendMessage(message);
    }
    /**
     * @do 正确弹窗
     * @author liuhua
     * @date 2020/3/12 9:19 PM
     */
    public static void alertOK(String msg) {
        Message message = Message.obtain(ClassUtil.get(HandleMessage.class), CountDownMsgTypeEnum.ALERT_MSG.getValue());
        Bundle data = message.getData();
        data.putString("msg",msg);
        data.putInt("type",SweetAlertDialog.SUCCESS_TYPE);
        data.putString("title","系统提示");
        ClassUtil.get(HandleMessage.class).sendMessage(message);
    }
    /**
     * @do 正确弹窗
     * @author liuhua
     * @date 2020/3/12 9:19 PM
     */
    public static void alertOther(SweetAlertDialog sweetAlertDialog) {
        Message message = Message.obtain(ClassUtil.get(HandleMessage.class), CountDownMsgTypeEnum.ALERT_SWEET.getValue());
        message.obj = sweetAlertDialog;
        ClassUtil.get(HandleMessage.class).sendMessage(message);
    }
    /**
     * @do 弹系统提示
     * @author liuhua
     * @date 2020/3/12 10:23 PM
     */
    public static void toast(String msg, int length) {
        Message message = Message.obtain(ClassUtil.get(HandleMessage.class), CountDownMsgTypeEnum.ALERT_TOAST.getValue());
        Bundle data = message.getData();
        data.putString("msg",msg);
        data.putInt("length",length);
        ClassUtil.get(HandleMessage.class).sendMessage(message);
    }
    /**
     * @do 进度条
     * @author liuhua
     * @date 2020/3/12 11:17 PM
     */
    public static SweetAlertDialog alertProcess(String ... text) {
        String title = "处理中";
        String content = "请稍候...";
        if(text!=null){
            if(text.length>0){
                title = text[0];
            }
            if(text.length>1){
                content = text[1];
            }
        }
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(ClassUtil.get(MainActivity.class), SweetAlertDialog.PROGRESS_TYPE)
                .setTitleText(title)
                .setContentText(content);
        sweetAlertDialog.setCancelable(false);
        Message message = Message.obtain(ClassUtil.get(HandleMessage.class), CountDownMsgTypeEnum.ALERT_SWEET.getValue());
        message.obj = sweetAlertDialog;
        ClassUtil.get(HandleMessage.class).sendMessage(message);
        return sweetAlertDialog;
    }
}

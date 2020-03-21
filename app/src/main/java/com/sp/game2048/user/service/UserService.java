package com.sp.game2048.user.service;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.sp.game2048.MainActivity;
import com.sp.game2048.R;
import com.sp.game2048.enums.CountDownMsgTypeEnum;
import com.sp.game2048.enums.ResultCodeEnum;
import com.sp.game2048.game.service.GameService;
import com.sp.game2048.handle.CallbackHandle;
import com.sp.game2048.handle.HandleMessage;
import com.sp.game2048.socket.entity.SocketMessage;
import com.sp.game2048.socket.enums.MsgHandleTypeEnum;
import com.sp.game2048.socket.enums.UserSocketCodeEnum;
import com.sp.game2048.socket.service.SocketService;
import com.sp.game2048.socket.util.SocketUtil;
import com.sp.game2048.user.enums.SmsTypeEnum;
import com.sp.game2048.util.AlertUtil;
import com.sp.game2048.util.ClassUtil;
import com.sp.game2048.util.HttpClientUtil;
import com.sp.game2048.util.ThreadPool;
import com.sp.game2048.util.UserUtil;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import cn.pedant.SweetAlert.SweetAlertDialog;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;

import static android.content.Context.MODE_PRIVATE;

/**
 * @do 用户业务处理
 * @author liuhua
 * @date 2020/3/13 5:26 PM
 */
public class UserService {
    /**
     * @do 用户注册
     * @author liuhua
     * @date 2020/3/11 9:24 PM
     */
    public void register() {
        MainActivity mainActivity = ClassUtil.get(MainActivity.class);
        View inflate = mainActivity.getLayoutInflater().inflate(R.layout.register, null);
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(ClassUtil.get(MainActivity.class), SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                .setTitleText("注册账号")
                .setCustomView(inflate)
                .setConfirmText("注册").setNeutralButton("已有账号?", sweetAlertDialog13 -> {
                    sweetAlertDialog13.cancel();
                    login();
                })
                .setCancelText("取消");
        sendSmsEvent(inflate, SmsTypeEnum.REGISTER.getValue());
        sweetAlertDialog.setCancelable(false);
        sweetAlertDialog.setConfirmClickListener(sweetAlertDialog1 -> {
            TextView viewById = inflate.findViewById(R.id.phone);
            String phone = viewById.getText().toString();
            if(!phone.matches("^[1][0-9]{10}$")){
                AlertUtil.toast("手机号码格式错误",Toast.LENGTH_SHORT);
                return;
            }
            viewById = inflate.findViewById(R.id.code);
            String code = viewById.getText().toString();
            if(!code.matches("^[0-9]{4,6}$")){
                AlertUtil.toast("验证码错误",Toast.LENGTH_SHORT);
                return;
            }
            viewById = inflate.findViewById(R.id.password);
            String password = viewById.getText().toString();
            if(password.length()<6||password.length()>20){
                AlertUtil.toast("密码格式需:6-20位",Toast.LENGTH_SHORT);
                return;
            }
            viewById = inflate.findViewById(R.id.password2);
            if(!password.equals(viewById.getText().toString())){
                AlertUtil.toast("两次密码输入不一致",Toast.LENGTH_SHORT);
                return;
            }
            SweetAlertDialog alertProcess = AlertUtil.alertProcess();
            FormBody formBody = new FormBody.Builder().add("password", password).add("phone", phone).add("code",code).build();
            HttpClientUtil.post("/show/user/register", formBody, new CallbackHandle(alertProcess) {
                @Override
                public void onSuccess(JSONObject jsonObject) {
                    if (ResultCodeEnum.OK.getValue().equals(jsonObject.getInteger("code"))) {
                        UserUtil.saveToken(jsonObject.getString("data"));
                        AlertUtil.toast("注册完成,自动登陆成功",Toast.LENGTH_SHORT);
                        sweetAlertDialog1.cancel();
                        init();
                    }else{
                        AlertUtil.toast(jsonObject.getString("msg"),Toast.LENGTH_SHORT);
                    }
                }
            });
        });
        AlertUtil.alertOther(sweetAlertDialog);
        return;
    }
    /**
     * @do 验证码倒计时
     * @author liuhua
     * @date 2020/3/21 1:40 PM
     */
    public void smsCodeTime(AppCompatButton view,Integer time){
        if(time<1){
            view.setText("发送验证码");
            view.setEnabled(true);
            return;
        }
        view.setText(String.format("%d秒后重发",time));
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            HandleMessage handleMessage = ClassUtil.get(HandleMessage.class);
            Message message = Message.obtain(handleMessage, CountDownMsgTypeEnum.CALL_BACK.getValue());
            message.obj = new Object[]{UserService.class,view,time-1};
            message.getData().putString("method","smsCodeTime");
            handleMessage.sendMessage(message);
        },1000);
    }
    /**
     * @do 用户登陆
     * @author liuhua
     * @date 2020/3/11 9:24 PM
     */
    public void login() {
        MainActivity mainActivity = ClassUtil.get(MainActivity.class);
        View inflate = mainActivity.getLayoutInflater().inflate(R.layout.user_login, null);
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(ClassUtil.get(MainActivity.class), SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                .setTitleText("快速登陆")
                .setCustomView(inflate)
                .setConfirmText("登陆").setNeutralButton("忘记密码?", sweetAlertDialog13 -> {
                    sweetAlertDialog13.cancel();
                    resetPassword();
                })
                .setCancelText("取消");
        sweetAlertDialog.setCancelable(false);
        sweetAlertDialog.setCancelClickListener(sweetAlertDialog12 -> {
            sweetAlertDialog12.cancel();
            AlertUtil.toast("取消了登陆,将不能使用部分功能", Toast.LENGTH_SHORT);
        });
        sweetAlertDialog.setConfirmClickListener(sweetAlertDialog1 -> {
            TextView viewById = inflate.findViewById(R.id.phone);
            String phone = viewById.getText().toString();
            if(!phone.matches("^[1][0-9]{10}$")){
                AlertUtil.toast("手机号码格式错误",Toast.LENGTH_SHORT);
                return;
            }
            viewById = inflate.findViewById(R.id.password);
            String password = viewById.getText().toString();
            if(password.length()<6||password.length()>20){
                AlertUtil.toast("密码错误",Toast.LENGTH_SHORT);
                return;
            }
            SweetAlertDialog alertProcess = AlertUtil.alertProcess();
            FormBody formBody = new FormBody.Builder().add("password", password).add("account", phone).build();
            HttpClientUtil.post("/user/login", formBody, new CallbackHandle(alertProcess) {
                @Override
                public void onSuccess(JSONObject jsonObject) {
                    if (ResultCodeEnum.OK.getValue().equals(jsonObject.getInteger("code"))) {
                        UserUtil.saveToken(jsonObject.getString("data"));
                        AlertUtil.toast("登陆成功",Toast.LENGTH_SHORT);
                        sweetAlertDialog1.cancel();
                        init();
                    }else{
                        AlertUtil.toast(jsonObject.getString("msg"),Toast.LENGTH_SHORT);
                    }
                }
            });
        });
        AlertUtil.alertOther(sweetAlertDialog);
        return;
    }

    /**
     * @do 刷新用户信息
     * @author liuhua
     * @date 2020/3/15 1:33 PM
     */
    public void refreshUserInfo(JSONObject jsonObject){
        MainActivity mainActivity = ClassUtil.get(MainActivity.class);
        SharedPreferences userInfo = mainActivity.getSharedPreferences("UserInfo", MODE_PRIVATE);
        if(jsonObject==null) {
            jsonObject = JSONObject.parseObject(userInfo.getString("userInfo","{}"));
        }else{
            SharedPreferences.Editor edit = userInfo.edit();
            edit.putString("userInfo", jsonObject.toJSONString());
            edit.apply();
        }
        String nickname = jsonObject.getString("nickname");
        if(nickname!=null) {
            TextView viewById = mainActivity.findViewById(R.id.nickname);
            viewById.setText(nickname);
            viewById = mainActivity.findViewById(R.id.nav_view).findViewById(R.id.nickname);
            if(viewById!=null) {
                //第一次可能还未初始完成
                viewById.setText(nickname);
            }
        }
    }
    /**
     * @do 更新昵称
     * @author liuhua
     * @date 2020/3/15 4:14 PM
     */
    public void updateNickname(String text) {
        SweetAlertDialog alertProcess = AlertUtil.alertProcess();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("nickname",text);
        ClassUtil.get(SocketService.class).sendMessage(MsgHandleTypeEnum.USER.getValue(), new SocketMessage(UserSocketCodeEnum.UPDATE_NICKNAME.getValue(),jsonObject), args -> {
            alertProcess.cancel();
            SocketMessage socketMessage = JSONObject.parseObject(args[0].toString(),SocketMessage.class);
            if(ResultCodeEnum.OK.getValue().equals(socketMessage.getCode())){
                //更新用户信息
                getUserInfo();
                AlertUtil.toast("更新昵称成功",Toast.LENGTH_SHORT);
            }else{
                AlertUtil.toast("更新昵称失败:"+socketMessage.getMsg(),Toast.LENGTH_SHORT);
            }
        });
    }
    /**
     * @do 进入软件后初始化
     * @author liuhua
     * @date 2020/3/15 10:17 PM
     */
    public void init() {
        String token = UserUtil.getToken();
        if(token==null){
            return;
        }
        //先使用原有数据显示
        refreshUserInfo(null);
        //连接socket
        ClassUtil.get(SocketService.class).connect(MsgHandleTypeEnum.USER.getValue(), () -> {
            //初始化排名
            ClassUtil.get(GameService.class).updateRanking(null,null);
            //获取用户信息
            getUserInfo();
        });
    }
    /**
     * @do 获取用户信息
     * @author liuhua
     * @date 2020/3/15 10:20 PM
     */
    private void getUserInfo(){
        SocketService socketService = ClassUtil.get(SocketService.class);
        socketService.sendMessage(MsgHandleTypeEnum.USER.getValue(), new SocketMessage(UserSocketCodeEnum.USER_INFO.getValue()), args -> {
            JSONObject socketMessage = JSONObject.parseObject(args[0].toString());
            HandleMessage handleMessage = ClassUtil.get(HandleMessage.class);
            Message message = Message.obtain(handleMessage, CountDownMsgTypeEnum.CALL_BACK.getValue());
            message.obj = new Object[]{UserService.class,socketMessage.getJSONObject("body")};
            message.getData().putString("method","refreshUserInfo");
            handleMessage.sendMessage(message);
        });
    }
    /**
     * @do 退出登陆
     * @author liuhua
     * @date 2020/3/15 10:53 PM
     */
    public void unLogin() {
        HttpClientUtil.post("/user/unlogin", new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
            }
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                ClassUtil.get(SocketService.class).disConnect();
            }
        });
        MainActivity mainActivity = ClassUtil.get(MainActivity.class);
        SharedPreferences userInfo = mainActivity.getSharedPreferences("UserInfo", MODE_PRIVATE);
        userInfo.edit().clear().apply();
        TextView viewById = mainActivity.findViewById(R.id.nickname);
        viewById.setText("(点击登陆)");
        View navView = mainActivity.findViewById(R.id.nav_view);
        viewById = navView.findViewById(R.id.nickname);
        viewById.setText("未登陆");
        viewById = navView.findViewById(R.id.ranking);
        viewById.setText("未上榜");
        viewById.setCompoundDrawables(null,null,null,null);
        viewById = navView.findViewById(R.id.score);
        viewById.setText("");

    }
    /**
     * @do 更新密码
     * @author liuhua
     * @date 2020/3/16 6:54 PM
     */
    public void updatePassword() {
        if(!ClassUtil.get(SocketService.class).isOnline()){
            AlertUtil.alertError("未登陆或网络未连接");
            return ;
        }
        MainActivity mainActivity = ClassUtil.get(MainActivity.class);
        View inflate = mainActivity.getLayoutInflater().inflate(R.layout.update_password, null);
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(ClassUtil.get(MainActivity.class), SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                .setTitleText("修改密码")
                .setCustomView(inflate)
                .setConfirmText("修改")
                .setCancelText("取消");
        sweetAlertDialog.setCancelable(false);
        sweetAlertDialog.setConfirmClickListener(sweetAlertDialog1 -> {
            sweetAlertDialog1.cancel();
            TextView viewById = inflate.findViewById(R.id.oldPassword);
            String oldPassword = viewById.getText().toString();
            if(StringUtils.isBlank(oldPassword)){
                AlertUtil.toast("密码格式错误",Toast.LENGTH_SHORT);
                return;
            }
            viewById = inflate.findViewById(R.id.password);
            String password = viewById.getText().toString();
            if(password.length()<6||password.length()>20){
                AlertUtil.toast("密码格式需:6-20位",Toast.LENGTH_SHORT);
                return;
            }
            viewById = inflate.findViewById(R.id.password2);
            if(!password.equals(viewById.getText().toString())){
                AlertUtil.toast("两次密码输入不一致",Toast.LENGTH_SHORT);
                return;
            }
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("password",password);
            jsonObject.put("oldPassword",oldPassword);
            SweetAlertDialog alertProcess = AlertUtil.alertProcess();
            ClassUtil.get(SocketService.class).sendMessage(MsgHandleTypeEnum.USER.getValue(), new SocketMessage(UserSocketCodeEnum.UPDATE_PASSWORD.getValue(),jsonObject), args -> {
                alertProcess.cancel();
                SocketMessage socketMessage = JSONObject.parseObject(args[0].toString(),SocketMessage.class);
                if(ResultCodeEnum.OK.getValue().equals(socketMessage.getCode())){
                    AlertUtil.toast("密码更新成功",Toast.LENGTH_SHORT);
                    sweetAlertDialog1.cancel();
                }else{
                    AlertUtil.toast("更新昵称失败:"+socketMessage.getMsg(),Toast.LENGTH_SHORT);
                }
            });
        });
        AlertUtil.alertOther(sweetAlertDialog);
    }
    /**
     * @do 重置密码
     * @author liuhua
     * @date 2020/3/11 9:24 PM
     */
    public void resetPassword() {
        MainActivity mainActivity = ClassUtil.get(MainActivity.class);
        View inflate = mainActivity.getLayoutInflater().inflate(R.layout.register, null);
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(ClassUtil.get(MainActivity.class), SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                .setTitleText("重置密码")
                .setCustomView(inflate)
                .setConfirmText("重置")
                .setCancelText("取消");
        sendSmsEvent(inflate,SmsTypeEnum.FIND.getValue());
        sweetAlertDialog.setCancelable(false);
        sweetAlertDialog.setConfirmClickListener(sweetAlertDialog1 -> {
            TextView viewById = inflate.findViewById(R.id.phone);
            String phone = viewById.getText().toString();
            if(!phone.matches("^[1][0-9]{10}$")){
                AlertUtil.toast("手机号码格式错误",Toast.LENGTH_SHORT);
                return;
            }
            viewById = inflate.findViewById(R.id.code);
            String code = viewById.getText().toString();
            if(!code.matches("^[0-9]{4,6}$")){
                AlertUtil.toast("验证码错误",Toast.LENGTH_SHORT);
                return;
            }
            viewById = inflate.findViewById(R.id.password);
            String password = viewById.getText().toString();
            if(password.length()<6||password.length()>20){
                AlertUtil.toast("密码格式需:6-20位",Toast.LENGTH_SHORT);
                return;
            }
            viewById = inflate.findViewById(R.id.password2);
            if(!password.equals(viewById.getText().toString())){
                AlertUtil.toast("两次密码输入不一致",Toast.LENGTH_SHORT);
                return;
            }
            SweetAlertDialog alertProcess = AlertUtil.alertProcess();
            FormBody formBody = new FormBody.Builder().add("password", password).add("phone", phone).add("code",code).build();
            HttpClientUtil.post("/show/user/resetPassword", formBody, new CallbackHandle(alertProcess) {
                @Override
                public void onSuccess(JSONObject jsonObject) {
                    if (ResultCodeEnum.OK.getValue().equals(jsonObject.getInteger("code"))) {
                        AlertUtil.toast("密码重置成功",Toast.LENGTH_SHORT);
                        sweetAlertDialog.cancel();
                    }else{
                        AlertUtil.toast(jsonObject.getString("msg"),Toast.LENGTH_SHORT);
                    }
                }
            });
        });
        AlertUtil.alertOther(sweetAlertDialog);
        return;
    }
    /**
     * @do 发送信息验证码
     * @author liuhua
     * @date 2020/3/21 4:21 PM
     */
    private void sendSmsEvent(View inflate,String type) {
        inflate.findViewById(R.id.send_code).setOnClickListener(v -> {
            v.setEnabled(false);
            TextView viewById = inflate.findViewById(R.id.phone);
            String phone = viewById.getText().toString();
            if(!phone.matches("^[1][0-9]{10}$")){
                AlertUtil.toast("手机号码格式错误", Toast.LENGTH_SHORT);
                v.setEnabled(true);
                return;
            }
            SweetAlertDialog alertProcess = AlertUtil.alertProcess("发送中");
            FormBody formBody = new FormBody.Builder().add("phone", phone).add("type",type).build();
            HttpClientUtil.post("/show/sms/sendSmsCode", formBody, new CallbackHandle(alertProcess) {
                @Override
                public void onSuccess(JSONObject jsonObject) {
                    if(!ResultCodeEnum.OK.getValue().equals(jsonObject.getInteger("code"))){
                        AlertUtil.toast(jsonObject.getString("msg"),Toast.LENGTH_SHORT);
                        HandleMessage handleMessage = ClassUtil.get(HandleMessage.class);
                        Message message = Message.obtain(handleMessage, CountDownMsgTypeEnum.CALL_BACK.getValue());
                        message.obj = new Object[]{UserService.class,v,0};
                        message.getData().putString("method","smsCodeTime");
                        handleMessage.sendMessage(message);
                        return;
                    }
                    AlertUtil.toast("发送成功",Toast.LENGTH_SHORT);
                    HandleMessage handleMessage = ClassUtil.get(HandleMessage.class);
                    Message message = Message.obtain(handleMessage, CountDownMsgTypeEnum.CALL_BACK.getValue());
                    message.obj = new Object[]{UserService.class,v,60};
                    message.getData().putString("method","smsCodeTime");
                    handleMessage.sendMessage(message);
                }

                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    super.onFailure(call, e);
                    v.setActivated(true);
                }
            });
        });
    }
}

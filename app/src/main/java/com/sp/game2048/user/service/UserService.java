package com.sp.game2048.user.service;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.sp.game2048.MainActivity;
import com.sp.game2048.R;
import com.sp.game2048.entity.PhoneInfo;
import com.sp.game2048.enums.CountDownMsgTypeEnum;
import com.sp.game2048.enums.ResultCodeEnum;
import com.sp.game2048.game.service.GameService;
import com.sp.game2048.handle.CallbackHandle;
import com.sp.game2048.handle.HandleMessage;
import com.sp.game2048.socket.entity.SocketMessage;
import com.sp.game2048.socket.enums.MsgHandleTypeEnum;
import com.sp.game2048.socket.enums.UserSocketCodeEnum;
import com.sp.game2048.socket.service.SocketService;
import com.sp.game2048.util.AlertUtil;
import com.sp.game2048.util.ClassUtil;
import com.sp.game2048.util.HttpClientUtil;
import com.sp.game2048.util.UserUtil;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

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
     * @do 用户登陆
     * @author liuhua
     * @date 2020/3/11 9:24 PM
     */
    public void login() {
        Context context = ClassUtil.get(MainActivity.class);
        final PhoneInfo phoneInfo = HttpClientUtil.phoneInfo;
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            AlertUtil.toast("登陆失败:没有权限获取到手机信息", Toast.LENGTH_LONG);
            return;
        }
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String mac = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            mac = tm.getImei();
        } else {
            mac = tm.getSimSerialNumber();
        }
        String phone = tm.getLine1Number();
        if(mac==null){
            mac = UserUtil.getMac();
        }
        if(mac==null){
            mac = phone;
        }
        if (phone == null) {
            AlertUtil.toast("登陆失败:无法获取到手机信息", Toast.LENGTH_LONG);
            return;
        }
        if (phone.length() > 11) {
            phone = phone.substring(phone.length() - 11);
        }
        phoneInfo.setPhone(phone);
        phoneInfo.setMac(mac);
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(ClassUtil.get(MainActivity.class), SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                .setTitleText("快速登陆")
                .setContentText(String.format("[%s]<br/>使用本机号码一键登陆码？", phone.replaceAll("^([0-9]{3}).*([0-9]{4})$", "$1****$2")))
                .setConfirmText("登陆")
                .setCancelText("取消");
        sweetAlertDialog.setCancelable(false);
        sweetAlertDialog.setCancelClickListener(sweetAlertDialog12 -> {
            sweetAlertDialog12.cancel();
            AlertUtil.toast("取消了登陆,将不能使用部分功能", Toast.LENGTH_SHORT);
        });
        sweetAlertDialog.setConfirmClickListener(sweetAlertDialog1 -> {
            sweetAlertDialog1.cancel();
            SweetAlertDialog alertProcess = AlertUtil.alertProcess();
            FormBody formBody = new FormBody.Builder().add("mac", phoneInfo.getMac()).add("phone", phoneInfo.getPhone()).build();
            HttpClientUtil.post("/show/user/g2048/login", formBody, new CallbackHandle(alertProcess) {
                @Override
                public void onSuccess(JSONObject jsonObject) {
                    if (ResultCodeEnum.OK.getValue().equals(jsonObject.getInteger("code"))) {
                        UserUtil.saveToken(jsonObject.getString("data"));
                        AlertUtil.toast("登陆成功",Toast.LENGTH_SHORT);
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
        MainActivity mainActivity = ClassUtil.get(MainActivity.class);
        LinearLayout linearLayout = new LinearLayout(mainActivity);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        EditText password1 = new EditText(mainActivity);
        password1.setSingleLine(true);
        password1.setTextSize(20f);
        password1.setGravity(Gravity.CENTER);
        password1.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        password1.setBackgroundResource(R.drawable.edit_style);
        password1.setHint("输入新密码");
        linearLayout.addView(password1);
        EditText password2 = new EditText(mainActivity);
        password2.setSingleLine(true);
        password2.setTextSize(20f);
        password2.setGravity(Gravity.CENTER);
        password2.setHint("再次输入新密码");
        password2.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        password2.setBackgroundResource(R.drawable.edit_style);
        linearLayout.addView(password2);
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(mainActivity);
        sweetAlertDialog.setTitle("修改密码");
        sweetAlertDialog.setCustomView(linearLayout);
        sweetAlertDialog.setConfirmText("确认修改");
        sweetAlertDialog.setConfirmClickListener(sweetAlertDialog1 -> {
            String p1 = password1.getText().toString();
            String p2 = password2.getText().toString();
            if(p1==null||p1.length()<6||p1.length()>20){
                AlertUtil.toast("密码格式错误",Toast.LENGTH_SHORT);
                return;
            }
            if(!p1.equals(p2)){
                AlertUtil.toast("两次密码不一致",Toast.LENGTH_SHORT);
                return;
            }
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("password",p1);
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
}

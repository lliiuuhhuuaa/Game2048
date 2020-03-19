package com.sp.game2048.handle;


import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.sp.game2048.enums.ResultCodeEnum;
import com.sp.game2048.util.AlertUtil;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import cn.pedant.SweetAlert.SweetAlertDialog;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public abstract class CallbackHandle implements Callback {
    private static final String TAG = "CallbackHandle";
    private boolean mustOk = false;
    private SweetAlertDialog alertProcess = null;
    public CallbackHandle(){}
    public CallbackHandle(boolean mustOk){
        this.mustOk = mustOk;
    }
    public CallbackHandle(SweetAlertDialog alertProcess){
        this.alertProcess = alertProcess;
    }
    @Override
    public void onFailure(@NotNull Call call, @NotNull IOException e) {
        Log.e(TAG, "onFailure: ",e );
        if(alertProcess!=null){
            alertProcess.cancel();
        }
        AlertUtil.alertError("服务连接错误");
    }

    @Override
    public void onResponse(@NotNull Call call, @NotNull Response response){
        if(alertProcess!=null){
            alertProcess.cancel();
        }
        try {
            JSONObject jsonObject = JSONObject.parseObject(response.body().string());
            if(jsonObject==null||!ResultCodeEnum.OK.getValue().equals(jsonObject.getInteger("code"))&&mustOk){
                AlertUtil.alertError(jsonObject==null?"服务连接错误":jsonObject.getString("msg"));
                return;
            }
            onSuccess(jsonObject);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * @do 成功返回
     * @author liuhua
     * @date 2020/3/12 9:09 PM
     */
    public abstract void onSuccess(JSONObject jsonObject);
}

package com.sp.game2048.util;

import android.content.SharedPreferences;

import com.sp.game2048.MainActivity;

import java.net.NetworkInterface;
import java.net.SocketException;

import static android.content.Context.MODE_PRIVATE;
/**
 * @do 用户工具
 * @author liuhua
 * @date 2020/3/13 5:23 PM
 */
public class UserUtil {
    /**
     * @do 获取登陆token
     * @author liuhua
     * @date 2020/3/13 5:22 PM
     */
    public static String getToken(){
        SharedPreferences userInfo = ClassUtil.get(MainActivity.class).getSharedPreferences("UserInfo", MODE_PRIVATE);
        return userInfo.getString("token",null);
    }
    /**
     * @do 保存登陆token
     * @author liuhua
     * @date 2020/3/13 5:22 PM
     */
    public static void saveToken(String token){
        SharedPreferences userInfo = ClassUtil.get(MainActivity.class).getSharedPreferences("UserInfo", MODE_PRIVATE);
        SharedPreferences.Editor edit = userInfo.edit();
        edit.putString("token",token);
        edit.apply();
    }
    /**
     * @do 获取mac地址
     * @author liuhua
     * @date 2020/3/19 7:30 PM
     */
    public static String getMac(){
        String macAddress = null;
        StringBuffer buf = new StringBuffer();
        NetworkInterface networkInterface = null;
        try {
            networkInterface = NetworkInterface.getByName("eth1");
            if (networkInterface == null) {
                networkInterface = NetworkInterface.getByName("wlan0");
            }
            if (networkInterface == null) {
                return "02:00:00:00:00:02";
            }
            byte[] addr = networkInterface.getHardwareAddress();
            for (byte b : addr) {
                buf.append(String.format("%02X:", b));
            }
            if (buf.length() > 0) {
                buf.deleteCharAt(buf.length() - 1);
            }
            macAddress = buf.toString();
        } catch (SocketException e) {
            e.printStackTrace();
            return "02:00:00:00:00:02";
        }
        return macAddress;
    }
}

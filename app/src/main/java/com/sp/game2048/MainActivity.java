package com.sp.game2048;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.google.android.material.navigation.NavigationView;
import com.sp.game2048.data.SQLiteData;
import com.sp.game2048.game.service.GameService;
import com.sp.game2048.handle.HandleMessage;
import com.sp.game2048.socket.service.SocketService;
import com.sp.game2048.user.service.UserService;
import com.sp.game2048.util.AlertUtil;
import com.sp.game2048.util.ClassUtil;
import com.sp.game2048.util.UserUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.ui.AppBarConfiguration;
import cn.pedant.SweetAlert.SweetAlertDialog;
import me.weyye.hipermission.HiPermission;
import me.weyye.hipermission.PermissionCallback;
import me.weyye.hipermission.PermissionItem;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private AppBarConfiguration mAppBarConfiguration;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Window window = getWindow();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        // 状态栏（以上几行代码必须，参考setStatusBarColor|setNavigationBarColor方法源码）
        window.setNavigationBarColor(Color.TRANSPARENT);
        //初始化
        init();
        //初始化绑定事件
        initEvent();
        //请求权限
        requestPermission();
        //显示用户信息
        ClassUtil.get(UserService.class).init();
    }
    /**
     * @do 绑定事件
     * @author liuhua
     * @date 2020/3/15 3:10 PM
     */
    private AtomicLong clickTime = new AtomicLong(0);
    private void initEvent() {
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        mAppBarConfiguration = new AppBarConfiguration.Builder()
                .setDrawerLayout(drawer)
                .build();
        findViewById(R.id.startGame).setOnClickListener(v -> {
            if(clickTime.get()+2000>System.currentTimeMillis()){
                return;
            }
            clickTime.set(System.currentTimeMillis());
            //开始游戏
            Intent intent1 = new Intent(MainActivity.this,
                    GameActivity.class);
            intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            MainActivity.this.startActivity(intent1);
        });
        NavigationView viewById = findViewById(R.id.nav_view);
        viewById.setNavigationItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId()){
                case R.id.quit_account:
                    ClassUtil.get(UserService.class).unLogin();
                    AlertUtil.toast("退出成功",Toast.LENGTH_SHORT);
                    drawer.closeDrawers();
                    break;
                case R.id.update_password:
                    ClassUtil.get(UserService.class).updatePassword();
                    break;
                case R.id.ranking:
                    drawer.closeDrawers();
                    ClassUtil.get(GameService.class).showRankingList();
                    break;
                case R.id.history:
                    drawer.closeDrawers();
                    ClassUtil.get(GameService.class).showScoreHistory();
                    break;
                case R.id.about:
                    drawer.closeDrawers();
                    AlertUtil.alertOK("六画出口,必属精品！！");
                    break;
            }
            return false;
        });
        //显示排行榜
        findViewById(R.id.ranking_list).setOnClickListener(v -> {
            ClassUtil.get(GameService.class).showRankingList();
        });
    }

    /**
     * @do 初始化
     * @author liuhua
     * @date 2020/3/11 10:38 PM
     */
    private void init() {
        ClassUtil.push(this);
        ClassUtil.push(new SQLiteData(), new HandleMessage(),new UserService(),new SocketService(),new GameService());
    }
    /**
     * @do 请求权限
     * @author liuhua
     * @date 2020/3/13 5:23 PM
     */
    private void requestPermission() {
        List<PermissionItem> permissionItems = new ArrayList<>();
       // permissionItems.add(new PermissionItem(Manifest.permission.READ_PHONE_STATE, "手机信息", R.drawable.permission_ic_phone));
        HiPermission.create(MainActivity.this)
                .title("权限申请")
                .msg("为了拥有更好的体验,请允许以下权限")
                .permissions(permissionItems)
                .checkMutiPermission(new PermissionCallback() {
                    @Override
                    public void onClose() {
                    }

                    @Override
                    public void onFinish() {
                       // ClassUtil.get(UserService.class).login();
                    }

                    @Override
                    public void onDeny(String permission, int position) {
                        Log.i(TAG, "onDeny");
                    }

                    @Override
                    public void onGuarantee(String permission, int position) {
                        Log.i(TAG, "onGuarantee");
                    }
                });
    }
    /**
     * @do 修改昵称
     * @author liuhua
     * @date 2020/3/15 3:29 PM
     */
    public void updateNickname(View view) {
        if(UserUtil.getToken()==null){
            return;
        }
        final EditText inputServer = new EditText(this);
        inputServer.setSingleLine(true);
        inputServer.setTextSize(20f);
        inputServer.setGravity(Gravity.CENTER);
        inputServer.setText(((TextView)view).getText());
        inputServer.setHint("输入昵称");
        inputServer.setBackgroundResource(R.drawable.edit_style);
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(this);
        sweetAlertDialog.setTitle("修改昵称");
        sweetAlertDialog.setCustomView(inputServer);
        sweetAlertDialog.setConfirmText("确认修改");
        sweetAlertDialog.setConfirmClickListener(sweetAlertDialog1 -> {
            String text = inputServer.getText().toString();
            if(text.length() < 2 || text.length() > 15){
                AlertUtil.toast("昵称格式:长度2-15位",Toast.LENGTH_SHORT);
                return;
            }
            ClassUtil.get(UserService.class).updateNickname(text);
            sweetAlertDialog1.cancel();
        });
        AlertUtil.alertOther(sweetAlertDialog);
    }
    /**
     * @do 检查并登陆
     * @author liuhua
     * @date 2020/3/15 11:47 PM
     */
    public void checkAndLogin(View view) {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        //打开菜单
        SharedPreferences userInfo = getSharedPreferences("UserInfo", MODE_PRIVATE);
        JSONObject jsonObject = JSONObject.parseObject(userInfo.getString("userInfo","{}"));
        if(jsonObject.getLong("userId")==null){
            ClassUtil.get(UserService.class).register();
        }else{
            drawer.openDrawer(GravityCompat.START);
            //显示用户信息
            ClassUtil.get(UserService.class).refreshUserInfo(null);
        }
    }
    private long firstClickBack = 0;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            long secondClickBack = System.currentTimeMillis();
            if (secondClickBack - firstClickBack > 1500) {
                Toast.makeText(this, "再一次确认并退出", Toast.LENGTH_SHORT).show();
                firstClickBack = secondClickBack;
                return true;
            } else {
                finish();
                System.exit(0);
            }
        }
        return super.onKeyDown(keyCode, event);
    }
    /**
     * @do 更新排名
     * @author liuhua
     * @date 2020/3/19 7:36 PM
     */
    public void updateRanking(View view) {
        ClassUtil.get(GameService.class).updateRanking(null,null);
    }
}

